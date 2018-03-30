package com.pxs.reaper.toolkit;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 29-03-2018
 */
public class ChildFirstClassLoader extends URLClassLoader {

    private ClassLoader parent;

    public ChildFirstClassLoader(final URL[] urls) {
        super(urls, null);
        this.parent = Thread.currentThread().getContextClassLoader();
        System.out.println("    System classloader        : " + ClassLoader.getSystemClassLoader());
        System.out.println("    System parent classloader : " + ClassLoader.getSystemClassLoader().getParent());
        System.out.println("    Context classloader       : " + Thread.currentThread().getContextClassLoader());
        System.out.println("    Class classloader         : " + ChildFirstClassLoader.class.getClassLoader());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            // Try the URL class loader first
            Class<?> aClass = super.findClass(name);
            System.out.println("    Child loaded : " + aClass + ", " + this.getClass().getClassLoader());
            return aClass;
        } catch (final ClassNotFoundException e) {
            // If not go to the parent
            Class<?> aClass = parent.loadClass(name);
            System.out.println("    Parent loaded : " + aClass + ", " + this.getClass().getClassLoader());
            return aClass;
        }
    }

}