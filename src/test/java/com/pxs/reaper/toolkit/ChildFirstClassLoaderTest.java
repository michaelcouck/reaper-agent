package com.pxs.reaper.toolkit;

import com.pxs.reaper.action.ReaperAgent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-03-2018
 */
public class ChildFirstClassLoaderTest {

    private ChildFirstClassLoader childFirstClassLoader;

    @Before
    @SuppressWarnings("ConstantConditions")
    public void before() throws MalformedURLException {
        File file = FILE.findFileRecursively(new File("."), "xpp3_min-1.1.4c.jar");
        String path = FILE.cleanFilePath(file.getAbsolutePath());
        URL[] urls = ReaperAgent.getClassPathUrls(path);
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
