package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.toolkit.ChildFirstClassLoader;
import com.pxs.reaper.toolkit.MANIFEST;
import com.pxs.reaper.toolkit.THREAD;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

/**
 * This class is attached to the running Java processes on the local operating system by the {@link com.pxs.reaper.Reaper}. It
 * in turn starts a timed task, {@link ReaperActionJvmMetrics}, object that collects various Java process telemetry and metrics
 * and posts the results to the reaper micro service.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
public class ReaperAgent {

    // private static boolean CLASS_PATH_ADDED_TO_SYSTEM_AND_BOOT = Boolean.FALSE;

    /**
     * TODO: Log bug report for linux. Passing arguments does not
     * TODO: work on linux, string concatenation logical error in the code.
     * <p>
     * JVM hook to dynamically load javaagent at runtime.
     * <p>
     * The agent class may have an agentmain method for use when the agent is
     * started after VM startup.
     *
     * @param args            any arguments from startup command
     * @param instrumentation the instrumentation instance from the JVM
     * @throws Exception anything happens to block the startup
     */
    @SuppressWarnings("WeakerAccess")
    public static void agentmain(final String args, final Instrumentation instrumentation) throws Exception {
        /*if (!CLASS_PATH_ADDED_TO_SYSTEM_AND_BOOT) {
            LOG.info("JsonReader loaded by : " + JsonReader.class.getClassLoader());
            CLASS_PATH_ADDED_TO_SYSTEM_AND_BOOT = Boolean.TRUE;
            Object[] jarPaths = getManifestClassPathUris();
            for (final Object jarPath : jarPaths) {
                JarFile jarFile = new JarFile(new File(jarPath.toString()));
                instrumentation.appendToSystemClassLoaderSearch(jarFile);
                instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
            }
        }*/

        String[] additionalClassPathUris = StringUtils.split(args, File.pathSeparator);
        System.out.println("Additional class path : " + Arrays.toString(additionalClassPathUris));
        URL[] urls = getClassPathUrls(additionalClassPathUris);
        if (!Thread.currentThread().getContextClassLoader().getClass().isAssignableFrom(ChildFirstClassLoader.class)) {
            URLClassLoader urlClassLoader = new ChildFirstClassLoader(urls);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
        }

        properties(args);
        premain(args, instrumentation);
    }

    private static void properties(final String args) {
        // If the properties are set(i.e. only on Windows) then set these first
        System.out.println("Arguments to agent : " + args);
        if (StringUtils.isEmpty(args)) {
            return;
        }
        String[] arguments = StringUtils.split(args, ";|");
        if (arguments == null || arguments.length == 0) {
            return;
        }
        for (final String argument : arguments) {
            if (StringUtils.isEmpty(argument)) {
                continue;
            }
            String[] argumentAndValue = StringUtils.split(argument, '=');
            if (argumentAndValue == null || argumentAndValue.length < 2) {
                continue;
            }
            System.setProperty(argumentAndValue[0], argumentAndValue[1]);
            System.out.println("Set system property from args : " + Arrays.toString(new Object[]{argumentAndValue[0] + "=" + argumentAndValue[1]}));
        }
    }

    /**
     * These instructions tell the JVM to call this method when loading class files.
     *
     * @param args            a set of arguments that the JVM will call the method with
     * @param instrumentation the instrumentation implementation of the JVM
     */
    public static void premain(final String args, final Instrumentation instrumentation) {
        final String pid = ManagementFactory.getRuntimeMXBean().getName();
        // Check if there is already an agent present
        if (Constant.AGENT_RUNNING.get()) {
            System.out.println("Agent already running : " + pid);
            // Do not start the actions running again in this JVM
            return;
        }
        System.out.println("Agent not running, starting... : " + pid);
        Constant.AGENT_RUNNING.set(Boolean.TRUE);
        Action timerTask = () -> {
            System.out.println("Starting the reaper agent in the target jvm : " + pid);
            int sleepTime = Constant.EXTERNAL_CONSTANTS.getSleepTime();
            ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
            THREAD.scheduleAtFixedRate(reaperActionJvmMetrics, sleepTime, sleepTime);
            Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
            System.out.println("Reaper agent successfully started in the target jvm : " + pid);
        };
        THREAD.schedule(timerTask, Constant.SLEEP_TIME);
    }

    public static URL[] getClassPathUrls(final String... additionalClassPathUris) {
        List<URL> classPathUrls = new ArrayList<>();
        Object[] manifestClassPathUris = getManifestClassPathUris();
        Object[] systemClassPathUris = getSystemClassPathUris();

        Object[] allClassPathUris =
                ArrayUtils.addAll(
                        additionalClassPathUris,
                        systemClassPathUris);

        Stream.of(additionalClassPathUris).forEach(s -> {
            try {
                // s = StringUtils.stripStart(StringUtils.strip(s, "file:/"), "file:\\");
                File file = new File(s);
                System.out.println("Path : " + s);
                URL url = new URL("file:" + File.separator + file.toString());
                classPathUrls.add(url);
                System.out.println("Added class path url : " + url + ", exists : " + file.exists());
            } catch (final Exception e) {
                System.out.println("Class path not correctly formed : " + s);
                e.printStackTrace();
            }
        });
        return classPathUrls.toArray(new URL[classPathUrls.size()]);
    }

    private static Object[] getSystemClassPathUris() {
        // System class path
        String classPath = System.getProperty("java.class.path");
        return classPath.split(File.pathSeparator);
    }

    private static Object[] getManifestClassPathUris() {
        // Manifest class path
        Object[] manifestClassPathUris = {};
        //noinspection ConstantConditions
        String libPath = "C:\\Users\\id851622\\workspace\\reaper-agent\\target\\lib"; // FILE.findFileRecursively(new File("."), Constant.LINUX_LOAD_MODULE).getParentFile().getAbsolutePath();
        System.out.println("Our library path : " + libPath);
        Manifest manifest = MANIFEST.getAgentManifest();
        System.out.println("Manifest : " + manifest);
        Attributes attributes = manifest.getMainAttributes();
        for (final Object key : attributes.keySet()) {
            String value = attributes.getValue(key.toString());
            System.out.println("Key : " + key + ", " + value);
            if (key.toString().contains("Class-Path")) {
                manifestClassPathUris = StringUtils.split(value, File.pathSeparator);
                for (int i = 0; i < manifestClassPathUris.length; i++) {
                    manifestClassPathUris[i] = libPath + File.pathSeparator + manifestClassPathUris[i];
                    System.out.println(manifestClassPathUris[i]);
                }
                break;
            }
        }
        return manifestClassPathUris;
    }

    /**
     * All kinds of opportunity here.
     */
    @SuppressWarnings({"WeakerAccess", "UnusedParameters"})
    public byte[] transform(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classBytes)
            throws IllegalClassFormatException {
        // Return the original bytes for the class
        System.out.println("Class loader for agent : " + loader);
        return classBytes;
    }

}