package org.onehippo.groovyrunner;

import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import java.util.UUID;


public class UpdaterInfoRegistry {

    private final static Logger log = LoggerFactory.getLogger(UpdaterInfoRegistry.class);

    private static final String UPDATE_PATH = "/hippo:configuration/hippo:update";
    private static final String UPDATE_REGISTRY_PATH = UPDATE_PATH + "/hippo:registry";
    private static final String UPDATE_QUEUE_PATH = UPDATE_PATH + "/hippo:queue";

    private static final long DEFAULT_BATCH_SIZE = 10l;
    private static final long DEFAULT_THOTTLE = 1000l;

    private static final String HIPPOSYS_UPDATERINFO = "hipposys:updaterinfo";

    Node node;
    Session session;
    String name;
    private String path;
    private String visitorPath;
    private String visitorQuery;
    private String method;
    private String batchSize;
    private String throttle;

    public UpdaterInfoRegistry(Node node) throws RepositoryException {
        this.node = node;
        this.session = node.getSession();

    }

    public Node getNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getDryRun() {
        return false;
    }

    public String copyToQueue(boolean randomizeNodeName) {
        if (validate()) {
            try {
                session.refresh(false);
                String srcPath = getPath();

                String newNodeName;
                if (randomizeNodeName) {
                    newNodeName = getName() + UUID.randomUUID().toString();
                } else {
                    newNodeName = getName();
                }
                final String destPath = UPDATE_QUEUE_PATH + "/" + newNodeName;
                JcrUtils.copy(session, srcPath, destPath);
                log.info("Copied updaterInfo " + srcPath + " to  " + destPath);
                final Node queuedNode = session.getNode(destPath);
                log.info("Run with dryrun is " + getDryRun());
                queuedNode.setProperty(HippoNodeType.HIPPOSYS_DRYRUN, getDryRun());
                queuedNode.setProperty(HippoNodeType.HIPPOSYS_STARTEDBY, session.getUserID());
                session.save();
                return newNodeName;
            } catch (RepositoryException e) {
                log.error("Unable to copy updaterinfo node" + e.getMessage());
                return null;
            }
        } else {
            log.error("Invalid updaterNode - Updater is not added to the updater queue");
            return null;
        }
    }

    private boolean validate() {
        try {
            loadProperties();

            if (isPathMethod()) {
                if (!validateVisitorPath()) {
                    return false;
                }
            } else {
                if (!validateVisitorQuery()) {
                    return false;
                }
            }

            return validateName() && validatePath() && isUpdater() && validateBatchSize() && validateThrottle();

        } catch (RepositoryException e) {
            final String message = "An unexpected error occurred: " + e.getMessage();
            log.error(message, e);
        }
        return false;
    }

    private void loadProperties() throws RepositoryException {
        visitorPath = getStringProperty(HippoNodeType.HIPPOSYS_PATH, null);
        visitorQuery = getStringProperty(HippoNodeType.HIPPOSYS_QUERY, null);
        if (visitorQuery != null) {
            method = "query";
        }
        if (visitorPath != null) {
            method = "path";
        }

        batchSize = String.valueOf(getLongProperty(HippoNodeType.HIPPOSYS_BATCHSIZE, DEFAULT_BATCH_SIZE));
        throttle = String.valueOf(getLongProperty(HippoNodeType.HIPPOSYS_THROTTLE, DEFAULT_THOTTLE));
    }

    protected final String getStringProperty(String propertyName, String defaultValue) {
        final Node node = getNode();
        if (node != null) {
            try {
                return JcrUtils.getStringProperty(node, propertyName, defaultValue);
            } catch (RepositoryException e) {
                log.error("Failed to retrieve property {}", propertyName, e);
            }
        }
        return defaultValue;
    }

    protected final Long getLongProperty(String propertyName, Long defaultValue) {
        final Node node = getNode();
        if (node != null) {
            try {
                return JcrUtils.getLongProperty(node, propertyName, defaultValue);
            } catch (RepositoryException e) {
                log.error("Failed to retrieve property {}", propertyName, e);
            }
        }
        return defaultValue;
    }

    private boolean validateName() {
        if (name == null || name.isEmpty()) {
            log.error("Name is empty");
            return false;
        }
        return true;
    }

    private boolean isUpdater() {
        if (node != null) {
            try {
                return node.isNodeType(HIPPOSYS_UPDATERINFO);
            } catch (RepositoryException e) {
                log.error("Failed to determine whether node is updater node", e);
            }
        }
        return false;
    }

    private boolean validatePath() {
        return path.startsWith(UPDATE_REGISTRY_PATH);
    }

    protected boolean validateVisitorPath() throws RepositoryException {
        if (visitorPath == null || visitorPath.isEmpty()) {
            log.error("Path is empty");
            return false;
        }
        try {
            if (!session.nodeExists(visitorPath)) {
                final String message = "The path does not exist";
                log.error(message);
                return false;
            }
        } catch (RepositoryException e) {
            final String message = "The path is not well-formed";
            log.error(message, e);
            return false;
        }
        return true;
    }

    private boolean validateVisitorQuery() throws RepositoryException {
        if (visitorQuery == null || visitorQuery.isEmpty()) {
            log.error("Query is empty");
            return false;
        }
        try {
            session.getWorkspace().getQueryManager().createQuery(visitorQuery, Query.XPATH);
        } catch (InvalidQueryException e) {
            final String message = "The query that is provided is not a valid xpath query";
            log.error(message, e);
            return false;
        }
        return true;
    }

    private boolean validateThrottle() {
        try {
            Long.valueOf(throttle);
            return true;
        } catch (NumberFormatException e) {
            log.error("Throttle must be a positive integer");
            return false;
        }
    }

    private boolean validateBatchSize() {
        try {
            Long.valueOf(batchSize);
            return true;
        } catch (NumberFormatException e) {
            log.error("Batch size must be a positive integer");
            return false;
        }
    }

    private boolean isPathMethod() {
        return method != null && method.equals("path");
    }


    public synchronized void cancel() {
        try {
            log.info("Cancelling copy of updater " + node.getName());
            session.refresh(false);
        } catch (RepositoryException e) {
            log.error("Cannot refresh session");
        }
    }
}
