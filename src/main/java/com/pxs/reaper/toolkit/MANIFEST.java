package com.pxs.reaper.toolkit;

import com.pxs.reaper.action.ReaperAgent;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class MANIFEST {

    private static final Logger LOGGER = Logger.getLogger(MANIFEST.class.getName());

    public static String getAgentClassPath() {
        Manifest manifest = getAgentManifest();
        Attributes attributes = manifest.getMainAttributes();
        for (final Object key : attributes.keySet()) {
            if (key.toString().equals("Class-Path")) {
                final String libPath = new File(getPathToAgent()).getParentFile().toString();
                final StringBuilder javaClassPath = new StringBuilder();
                String manifestClassPath = attributes.getValue(Attributes.Name.class.cast(key));
                String[] manifestClassPathEntries = StringUtils.split(manifestClassPath, "\r\n " + File.pathSeparator + File.separator);
                Stream.of(manifestClassPathEntries).forEach(s -> {
                    javaClassPath.append(libPath);
                    javaClassPath.append(File.separator);
                    javaClassPath.append(s);
                    javaClassPath.append(File.pathSeparator);
                });
                System.out.println("Manifest class path entries : " + javaClassPath);
                return javaClassPath.toString();
            }
        }
        return null;
    }

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

    /**
     * Access to the jar that contains the {@link ReaperAgent}. The path is necessary to attach dynamically to Java processes.
     *
     * @return the absolute, cleaned, normalized, canonical path to the jar containing 'this' Java agent
     */
    public static String getPathToAgent() {
        String agentJarName = null;
        Attributes attrs = MANIFEST.getAgentManifest().getMainAttributes();
        for (final Object key : attrs.keySet()) {
            String value = attrs.getValue(Attributes.Name.class.cast(key));
            if (key.toString().equals("Agent-Jar-Name") && value.contains("reaper-agent-")) {
                agentJarName = value;
            }
        }

        File agentJar = FILE.findFileRecursively(new File("."), agentJarName);
        if (agentJar == null || !agentJar.exists()) {
            return "./";
        }
        return FILE.cleanFilePath(agentJar.getAbsolutePath());
    }

}
