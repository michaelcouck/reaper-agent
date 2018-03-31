package com.pxs.reaper.agent.toolkit;

import com.pxs.reaper.agent.action.ReaperAgent;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This class opens the manifest and extracts whatever is needed for running the logic. Access to the
 * class loader when the agent is first initialized, is through the App and Ext class loaders, in that order,
 * i.e. the system then the boot class loaders. Consequently it is not
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-03-2018
 */
public class MANIFEST {

    private static final Logger LOGGER = Logger.getLogger(MANIFEST.class.getName());

    @SuppressWarnings("WeakerAccess")
    public static URL[] getAgentClassPath() {
        Manifest manifest = getAgentManifest();
        Attributes attributes = manifest.getMainAttributes();

        List<URL> javaClassPath = new ArrayList<>();
        String pathToAgent = FILE.cleanFilePath(new File(getPathToAgent()).getParentFile().getAbsolutePath());
        try {
            javaClassPath.add(new URL("file:/" + getPathToAgent()));
        } catch (final MalformedURLException e) {
            LOGGER.log(Level.SEVERE, "Exception adding agent jar to class path : ", e);
        }

        attributes.keySet().stream().filter(key -> key.toString().equals("Class-Path")).forEach(key -> {
            String manifestClassPath = attributes.getValue(Attributes.Name.class.cast(key));
            String[] manifestClassPathEntries = StringUtils.split(manifestClassPath, "\r\n" + " " + File.pathSeparator);
            Stream.of(manifestClassPathEntries).forEach(s -> {
                try {
                    URL classPathUrl = new URL("file:/" + pathToAgent + File.separator + s);
                    LOGGER.log(Level.FINE, "    class path url : " + classPathUrl);
                    javaClassPath.add(classPathUrl);
                } catch (final MalformedURLException e) {
                    LOGGER.log(Level.SEVERE, "Exception reading the manifest : ", e);
                }
            });
        });
        return javaClassPath.toArray(new URL[javaClassPath.size()]);
    }

    @SuppressWarnings("WeakerAccess")
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
                break;
            }
        }

        File agentJar = FILE.findFileRecursively(new File("."), agentJarName);
        if (agentJar == null || !agentJar.exists()) {
            try {
                String agentJarPath = MANIFEST.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                LOGGER.log(Level.INFO, "Own jar path : " + agentJarPath);
                return FILE.cleanFilePath(new File(agentJarPath).getAbsolutePath());
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Exception looking up the path to the jar source : ", e);
            }
            return "./";
        }

        return FILE.cleanFilePath(agentJar.getAbsolutePath());
    }

    @SuppressWarnings("UnusedParameters")
    public static URL[] getClassPathUrls(final String... additionalClassPathUris) {
        List<URL> classPathUrls = new ArrayList<>();
        classPathUrls.addAll(Arrays.asList(getSystemClassPathUris()));
        classPathUrls.addAll(Arrays.asList(MANIFEST.getAgentClassPath()));
        return classPathUrls.toArray(new URL[classPathUrls.size()]);
    }

    private static URL[] getSystemClassPathUris() {
        Set<URL> urls = new TreeSet<>((o1, o2) -> o1.toString().compareTo(o2.toString()));

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        System.out.println("Class loader from target system : " + classLoader);
        urls.addAll(Arrays.asList(((URLClassLoader) classLoader).getURLs()));

        ClassLoader classLoaderParent = classLoader.getParent();
        if (classLoaderParent != null) {
            System.out.println("Parent class loader from target system : " + classLoaderParent);
            urls.addAll(Arrays.asList(((URLClassLoader) classLoaderParent).getURLs()));
        }

        String classPath = System.getProperty("java.class.path"); // Add this too? Redundant?
        System.out.println("Class path from properties : " + classPath);

        return urls.toArray(new URL[urls.size()]);
    }

}