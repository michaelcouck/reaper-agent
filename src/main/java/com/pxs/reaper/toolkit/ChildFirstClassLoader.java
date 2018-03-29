package com.pxs.reaper.toolkit;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Logger;

public class ChildFirstClassLoader extends URLClassLoader {

    private static Logger LOG = Logger.getLogger(ChildFirstClassLoader.class.getSimpleName());

    private ClassLoader parent;

    public ChildFirstClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, null);
        this.parent = parent;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            // Try the URL class loader first
            return super.findClass(name);
        } catch (final ClassNotFoundException e) {
            // If not go to he parent
            return parent.loadClass(name);
        }
    }

}
