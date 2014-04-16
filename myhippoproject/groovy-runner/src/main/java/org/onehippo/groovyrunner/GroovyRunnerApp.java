package org.onehippo.groovyrunner;

import org.onehippo.groovyrunner.utils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application for starting the inspector of the repository
 */
public class GroovyRunnerApp {

    private static final Logger log = LoggerFactory.getLogger(GroovyRunnerApp.class);
    private static final String DEFAULT_CONFIG_FILE = "runner.properties";

    public static void main(String[] args) {

        log.info("Configuration GroovyRunnerApp");
        UpdaterInspectorConfig config = parseConfig(args);

        log.info("Connect GroovyRunnerApp");
        if (config == null) {
            log.error("Configuration not initialized");
            System.exit(1);
        }

        //Register hook for proper shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());

        JcrHelper.setServerUrl(config.getRepositoryUrl());
        JcrHelper.setUsername(config.getRepositoryUser());
        JcrHelper.setPassword(config.getRepositoryPass());
        JcrHelper.connect();
        log.info("Start GroovyRunnerApp");

        GroovyRunner groovyRunner = new GroovyRunner(config.getUpdaters());
        groovyRunner.start();

        if (config.isWaitUntilDone()) {
            waitUntilUpdatersAreFinished(config, groovyRunner);
        } else {
            log.info("Not waiting for updaters to finish executing, exiting..");
        }
    }

    private static void waitUntilUpdatersAreFinished(final UpdaterInspectorConfig config, final GroovyRunner groovyRunner) {
        long interval = 1000 * 2;
        while (!groovyRunner.isDone()) {
            try {
                log.info("Updaters not done yet, waiting {}ms", interval);
                Thread.sleep(interval);
                interval = Math.min(config.getMaxSleepInterval() * 1000, interval * 2);
            } catch (InterruptedException e) {
                log.info("Interrupted while waiting for updaters to finish running");
                break;
            }
        }
    }

    /**
     * Parse arguments from command line in order to retrieve properties file with absolute path. If no arguments are
     * passed, the default properties file is retrieved which should be on the classpath (in the jar).
     *
     * @param args String array with command line arguments
     * @return UpdaterInspectorConfig with properties initialized and validated.
     */
    static UpdaterInspectorConfig parseConfig(String[] args) {
        InputStream inputStream = null;
        try {
            Properties props = new Properties();
            log.info("parseConfig arguments {} ", args);
            if (args != null && args.length > 0) {
                for (String arg : args) {
                    inputStream = readFile(arg);
                    if (inputStream != null) {
                        props.load(inputStream);
                    } else {
                        log.error("No arguments loaded because of exception");
                        System.exit(1);
                    }
                }
                log.info("props {} ", props.stringPropertyNames());
                return new UpdaterInspectorConfig(props);
            } else {
                log.info("parseConfig GroovyRunnerApp");
                return new UpdaterInspectorConfig(readFile(DEFAULT_CONFIG_FILE));
            }
        } catch (Exception e) {
            log.error("Exception occurred during parsing parameters {}", e.getMessage());
            System.exit(1);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return null;
    }

    static class ShutdownHook extends Thread {
        public void run() {
            JcrHelper.disconnect();
        }
    }

    static InputStream readFile(String fileName) {
        InputStream inputStream = null;
        try {
            inputStream = FileUtils.readFileToStream(fileName);
        } catch (IOException e) {
            log.error("IOexception occured {}", e.getMessage());
            System.exit(1);
        }
        return inputStream;
    }

}
