package com.pxs.reaper.toolkit;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MANIFEST {

    private static final Logger LOGGER = Logger.getLogger(MANIFEST.class.getName());

    public static Manifest getAgentManifest() {
        Enumeration<URL> resources;
        try {
            resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/MANIFEST.MF");
        } catch (final IOException e) {
            throw new RuntimeException("Exception reading the manifest files : ", e);
        }
        //noinspection ConstantConditions
        while (resources.hasMoreElements()) {
            URL url = null;
            try {
                url = resources.nextElement();
                Manifest manifest = new Manifest(url.openStream());
                Attributes attrs = manifest.getMainAttributes();
                for (final Object key : attrs.keySet()) {
                    String value = attrs.getValue(Attributes.Name.class.cast(key));
                    if (key.toString().equals("Agent-Jar-Name") && value.contains("reaper-agent-")) {
                        return manifest;
                    }
                }
            } catch (final IOException e) {
                LOGGER.log(Level.SEVERE, "Exception reading the manifest : " + url, e);
            }
        }
        throw new RuntimeException("Couldn't find our own manifest?");
    }

}
