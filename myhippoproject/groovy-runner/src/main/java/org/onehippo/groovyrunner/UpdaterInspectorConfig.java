package org.onehippo.groovyrunner;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class UpdaterInspectorConfig {

    private static final Logger log = LoggerFactory.getLogger(UpdaterInspectorConfig.class);

    protected static final String REPOSITORY_URL = "repository.url";
    protected static final String REPOSITORY_USER = "repository.user";
    protected static final String REPOSITORY_PASS = "repository.pass";
    protected static final String GROOVY_SCRIPTS = "groovy.scripts";
    private static final String WAIT_UNTIL_DONE = "wait.until.done";
    private static final String MAX_SLEEP_INTERVAL = "max.sleep.interval";
    private static final String SEPERATOR = ",";

    private String repositoryUrl;
    private String repositoryUser;
    private String repositoryPass;
    private String groovyScripts;
    private List<String> updaters;
    private boolean waitUntilDone;
    private int maxSleepInterval;

    public UpdaterInspectorConfig(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            throw new IllegalArgumentException("Stream is null");
        }

        Properties properties = new Properties();
        properties.load(inputStream);
        initialize(properties);
    }

    public UpdaterInspectorConfig(Properties properties) {
        initialize(properties);
    }

    private void initialize(Properties properties) {
        log.info("Initialize UpdaterInspectorConfig");
        readUpdaterInspectorConfig(properties);
        if (!isValid()) {
            throw new IllegalArgumentException("Configuration properties are not correct");
        }
        setUpdaters(convertToList(getGroovyScripts(), SEPERATOR));
    }

    public boolean isWaitUntilDone() {
        return waitUntilDone;
    }

    public void setWaitUntilDone(final boolean waitUntilDone) {
        this.waitUntilDone = waitUntilDone;
    }

    public int getMaxSleepInterval() {
        return maxSleepInterval;
    }

    public void setMaxSleepInterval(final int maxSleepInterval) {
        this.maxSleepInterval = maxSleepInterval;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getRepositoryUser() {
        return repositoryUser;
    }

    public void setRepositoryUser(String repositoryUser) {
        this.repositoryUser = repositoryUser;
    }

    public String getRepositoryPass() {
        return repositoryPass;
    }

    public void setRepositoryPass(String repositoryPass) {
        this.repositoryPass = repositoryPass;
    }

    public String getGroovyScripts() {
        return groovyScripts;
    }

    public void setGroovyScripts(String groovyScripts) {
        this.groovyScripts = groovyScripts;
    }

    private boolean isValid() {

        if (isEmpty(getRepositoryUrl())) {
            log.error("repository.url is missing.");
            return false;
        }
        if (isEmpty(getRepositoryUser())) {
            log.error("repository.user is missing.");
            return false;
        }
        if (isEmpty(getRepositoryPass())) {
            log.error("repository.pass is missing.");
            return false;
        }

        return isValidGroovyScripts(getGroovyScripts(), SEPERATOR);

    }

    private boolean isEmpty(String s) {
        return s == null || "".equals(s);
    }

    private void readUpdaterInspectorConfig(Properties properties) {
        setRepositoryUrl(properties.getProperty(REPOSITORY_URL));
        setRepositoryUser(properties.getProperty(REPOSITORY_USER));
        setRepositoryPass(properties.getProperty(REPOSITORY_PASS));
        setGroovyScripts(properties.getProperty(GROOVY_SCRIPTS));
        setWaitUntilDone(Boolean.parseBoolean(properties.getProperty(WAIT_UNTIL_DONE)));
        setMaxSleepInterval(Integer.parseInt(properties.getProperty(MAX_SLEEP_INTERVAL, "300")));
    }

    private boolean isValidGroovyScripts(String s, String seperator) {
        return StringUtils.isNotEmpty(s) && s.contains(seperator);
    }

    public List<String> getUpdaters() {
        return updaters;
    }

    public void setUpdaters(List<String> updaters) {
        this.updaters = updaters;
    }

    private List<String> convertToList(String s, String seperator) {
        List<String> stringList = new ArrayList<String>();
        String[] sArr = s.split(seperator);
        for (String ss : sArr) {
            String ts = StringUtils.trimToEmpty(ss);
            if (StringUtils.isNotEmpty(ts)) {
                stringList.add(ts);
            }
        }
        return stringList;
    }

}
