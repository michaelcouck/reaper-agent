package com.pxs.reaper.toolkit;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-03-2018
 */
public class ChildFirstClassLoader extends URLClassLoader {

    private Logger logger = Logger.getLogger(ChildFirstClassLoader.class.getSimpleName());

    private ClassLoader parent;

    public ChildFirstClassLoader(final URL[] urls) {
        super(urls, null);
        this.parent = Thread.currentThread().getContextClassLoader();
        logger.info("System classloader        : " + ClassLoader.getSystemClassLoader());
        logger.info("System parent classloader : " + ClassLoader.getSystemClassLoader().getParent());
        logger.info("Context classloader       : " + Thread.currentThread().getContextClassLoader());
        logger.info("Class classloader         : " + ChildFirstClassLoader.class.getClassLoader());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            // Try the URL class loader first
            Class<?> aClass = super.findClass(name);
            logger.info("Url loaded class : " + aClass);
            return aClass;
        } catch (final ClassNotFoundException e) {
            // If not go to the parent
            Class<?> aClass = parent.loadClass(name);
            logger.info("Url loaded class : " + aClass);
            return aClass;
        }
    }

}