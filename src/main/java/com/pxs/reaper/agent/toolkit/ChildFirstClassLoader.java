package com.pxs.reaper.agent.toolkit;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * This is a child first class loader to isolate some of the agent from the rest of the target application.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-03-2018
 */
public class ChildFirstClassLoader extends URLClassLoader {

    private ClassLoader parent;

    public ChildFirstClassLoader(final URL[] urls) {
        super(urls, null);
        this.parent = ClassLoader.getSystemClassLoader().getParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        try {
            // Try the URL class loader first
            return super.findClass(name);
        } catch (final Exception e) {
            // If not go to the parent
            return parent.loadClass(name);
        }
    }

}