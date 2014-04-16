package org.onehippo.groovyrunner;


import org.apache.jackrabbit.rmi.client.ClientRepositoryFactory;
import org.apache.jackrabbit.rmi.client.RemoteRepositoryException;
import org.apache.jackrabbit.rmi.client.RemoteRuntimeException;
import org.hippoecm.repository.HippoRepository;
import org.hippoecm.repository.HippoRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


/**
 * Helper class for JCR
 */
public final class JcrHelper {

    private static final Logger log = LoggerFactory.getLogger(JcrHelper.class);

    private static String server;

    private static String username;

    private static char[] password;

    private static Session session;

    private static boolean connected;

    private static boolean isHippoRepository = true;

    private JcrHelper() {}

    public static boolean isHippoRepository() {
        return isHippoRepository;
    }

    public static boolean isConnected() {
        try {
            if (session != null && session.isLive()) {
                return true;
            }
        } catch (RemoteRuntimeException e) {
            log.error("Error communicating with server. ", e);
            setConnected(false);
        }
        return connected;
    }

    public static void setConnected(final boolean connected) {
        JcrHelper.connected = connected;
    }

    public static char[] getPassword() {
        return password.clone();
    }

    public static void setPassword(final String password) {
        JcrHelper.password = password.toCharArray();
    }

    public static String getServer() {
        return server;
    }

    public static void setServerUrl(final String server) {
        JcrHelper.server = server;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(final String username) {
        JcrHelper.username = username;
    }

    public static boolean connect() {
        if (isConnected()) {
            return true;
        }
        // get the repository login and get session
        try {
            if (isHippoRepository()) {
                log.info("Connecting to Hippo Repository at '" + getServer() + "' : ");
                HippoRepository repository = HippoRepositoryFactory.getHippoRepository(getServer());
                session = repository.login(new SimpleCredentials(getUsername(), getPassword()));
            } else {
                log.info("Connecting to JCR Repository at '" + getServer() + "' : ");
                ClientRepositoryFactory factory = new ClientRepositoryFactory();
                Repository repository = factory.getRepository(getServer());
                session = repository.login(new SimpleCredentials(getUsername(), getPassword()));
            }
            setConnected(true);
            log.debug("Connected.");
            return true;
        } catch (RemoteRepositoryException e) {
            log.error("Remote error while connection to server: " + getServer(), e);
        } catch (LoginException e) {
            log.error("Unable to login to server: " + getServer(), e);
        } catch (RepositoryException e) {
            log.error("Error while connection to server: " + getServer(), e);
        } catch (MalformedURLException e) {
            log.error("Invalid connection url: " + getServer(), e);
        } catch (ClassCastException e) {
            log.error("ClassCastException while connection to server: " + getServer(), e);
        } catch (RemoteException e) {
            log.error("RemoteException while connection to server: " + getServer(), e);
        } catch (NotBoundException e) {
            log.error("Server not found in rmi lookup: " + getServer(), e);
        }
        return false;
    }

    public static void disconnect() {
        if (isConnected()) {
            log.info("Disconnecting from '" + getServer() + "' : ");
            session.logout();
            log.debug("Disconnected.");
            setConnected(false);
        }
    }

    public static Node getRootNode() throws RepositoryException {
        return session.getRootNode();
    }

}
