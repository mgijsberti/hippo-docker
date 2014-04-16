package org.onehippo.groovyrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;

public class GroovyRunner {

    private static final Logger log = LoggerFactory.getLogger(GroovyRunner.class);
    private static final String REGISTRY_QUERY = "/jcr:root/hippo:configuration/hippo:update/hippo:registry//element(*,hipposys:updaterinfo)";
    private static final String HISTORY_NODE = "/hippo:configuration/hippo:update/hippo:history";
    private static final String XPATH = "xpath";

    private final List<String> updaters;
    private final List<String> queuedUpdaters = new ArrayList<String>();

    public GroovyRunner(List<String> updaters) {
        this.updaters = updaters;
    }

    public void start() {

        try {
            String updaters = parseUpdaters();
            log.info("GroovyRunner will start the scripts : {}", updaters);
            Session session = JcrHelper.getRootNode().getSession();

            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query jcrQuery = queryManager.createQuery(REGISTRY_QUERY, XPATH);
            QueryResult results = jcrQuery.execute();

            NodeIterator nodes = results.getNodes();
            while (nodes.hasNext()) {
                final Node node = nodes.nextNode();
                log.info("Visit Groovy script {} ", node.getName());
                UpdaterInfoRegistry updaterInfoRegistry = new UpdaterInfoRegistry(node);
                updaterInfoRegistry.setName(node.getName());
                updaterInfoRegistry.setPath(node.getPath());
                if (updaters.contains(updaterInfoRegistry.getName())) {
                    log.info("Bootstrap Groovy script {} ", updaterInfoRegistry.getName());
                    String queuedUpdaterName = updaterInfoRegistry.copyToQueue(true);
                    queuedUpdaters.add(queuedUpdaterName);
                } else {
                    log.info("Updater {} is skipped", updaterInfoRegistry.getName());
                }
            }

        } catch (RepositoryException e) {
            log.error("Error during start updaters {}", e.getMessage());
        }

    }

    private String parseUpdaters() {
        StringBuilder builder = new StringBuilder();
        for (String updater : updaters) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(updater);
        }
        return builder.toString();
    }

    public boolean isDone() {
        List<String> unfinishedUpdaters = new ArrayList<String>();
        try {
            Session session = JcrHelper.getRootNode().getSession();
            Node updaterHistoryNode = session.getNode(HISTORY_NODE);
            for (String queuedUpdaterName : queuedUpdaters) {
                if (!updaterHistoryNode.hasNode(queuedUpdaterName)) {
                    unfinishedUpdaters.add(queuedUpdaterName);
                }
            }
            if (unfinishedUpdaters.isEmpty()) {
                log.info("All updaters are finished");
                return true;
            } else {
                log.info("Unfinished updaters: {}", unfinishedUpdaters);
                return false;
            }
        } catch (RepositoryException e) {
            log.error("Error while trying to determine if updaters are finished", e);
            System.exit(2);
        }
        return unfinishedUpdaters.isEmpty();
    }
}
