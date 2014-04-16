package org.onehippo.groovyrunner.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class FileUtils {

    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    public static InputStream readFileToStream(String fileName) throws IOException {
        log.info("Read file {} ", fileName);
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        return new BufferedInputStream(fis);
    }
}
