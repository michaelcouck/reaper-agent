package com.pxs.reaper.agent.toolkit;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-03-2018
 */
public class ChildFirstClassLoaderTest {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private ChildFirstClassLoader childFirstClassLoader;

    @Before
    public void before() throws MalformedURLException {
        File dotDirectory = new File(".");
        logger.severe("Dot directory : " + dotDirectory.getAbsolutePath());
        File file = FILE.findFileRecursively(new File("."), "xpp3_min-1.1.4c.jar");

        assert file != null;
        String path = FILE.cleanFilePath(file.getAbsolutePath());
        URL[] urls = MANIFEST.getClassPathUrls(path);
        urls = ArrayUtils.addAll(urls, file.toURI().toURL());
        childFirstClassLoader = new ChildFirstClassLoader(urls);
    }

    @Test
    public void findClass() throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            String classFile = "org.xmlpull.mxp1.MXParser";
            Thread.currentThread().setContextClassLoader(childFirstClassLoader);
            // childFirstClassLoader.findClass(classFile);
            Class<?> customLoadedClass = Thread
                    .currentThread()
                    .getContextClassLoader()
                    .loadClass(classFile);
            Assert.assertNotNull(customLoadedClass);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

}
