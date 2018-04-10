package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.Reaper;
import com.pxs.reaper.agent.action.instrumentation.SocketClassFileTransformer;
import com.pxs.reaper.agent.toolkit.ChildFirstClassLoader;
import com.pxs.reaper.agent.toolkit.MANIFEST;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

/**
 * This class is attached to the running Java processes on the local operating system by the {@link Reaper}. It
 * in turn starts a timed task, {@link ReaperActionJvmMetrics}, object that collects various Java process telemetry and metrics
 * and posts the results to the reaper micro service.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
public class ReaperAgent {

    private static final AtomicBoolean LOADED = new AtomicBoolean(Boolean.FALSE);

    /**
     * Note to self, don't use anything in here other than java base classes.
     * <p>
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
        System.out.println("        Agent main : ");
        premain(args, instrumentation);
    }

    /**
     * These instructions tell the JVM to call this method when loading class files.
     *
     * @param args            a set of arguments that the JVM will call the method with
     * @param instrumentation the instrumentation implementation of the JVM
     */
    public static void premain(final String args, final Instrumentation instrumentation) {
        synchronized (LOADED) {
            if (LOADED.get()) {
                System.out.println("        Pre-main already called : ");
                return;
            }
            LOADED.set(Boolean.TRUE);
        }
        System.out.println("        Pre main : ");

        try {
            // instrumentation.appendToSystemClassLoaderSearch(new JarFile(MANIFEST.getPathToAgent()));
            // This causes a linkage error in a target jvm but works in the unit test
            // instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(MANIFEST.getPathToAgent()));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        // Do any transformation that we need
        /*instrumentation.addTransformer(new SocketClassFileTransformer(), true);
        try {
            Class clazz = Socket.class;
            String className = clazz.getName();
            String classAsPath = className.replace('.', '/') + ".class";
            InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(classAsPath);
            byte[] classFileBytes = IOUtils.toByteArray(stream);

            instrumentation.retransformClasses(Socket.class, SocketImpl.class);
            instrumentation.redefineClasses(new ClassDefinition(clazz, classFileBytes));
        } catch (final UnmodifiableClassException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }*/

        final String pid = ManagementFactory.getRuntimeMXBean().getName();
        new Thread(() -> {
            System.out.println("        Starting the reaper agent in the target jvm : " + pid);
            URL[] urls = MANIFEST.getClassPathUrls();
            System.out.println("        URLs for class loader : " + Arrays.toString(urls));
            URLClassLoader urlClassLoader = new ChildFirstClassLoader(urls);

            /*Field scl = null; // Get system class loader
            try {
                scl = ClassLoader.class.getDeclaredField("scl");
                scl.setAccessible(true); // Set accessible
                scl.set(null, urlClassLoader); // Update it to your class loader
            } catch (final NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }*/

            Thread.currentThread().setContextClassLoader(urlClassLoader);
            ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
            Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
            System.out.println("        Started the reaper agent in the target jvm : " + pid);
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    Thread.sleep(Constant.SLEEP_TIME);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                reaperActionJvmMetrics.run();
            }
        }).start();

        /*Action startupAction = () -> {
            URL[] urls = MANIFEST.getClassPathUrls();
            if (!Thread.currentThread().getContextClassLoader().getClass().isAssignableFrom(ChildFirstClassLoader.class)) {
                URLClassLoader urlClassLoader = new ChildFirstClassLoader(urls);
                Thread.currentThread().setContextClassLoader(urlClassLoader);
            }
            System.out.println("        Starting the reaper agent in the target jvm : " + pid);
            int sleepTime = (int) Constant.SLEEP_TIME;
            ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
            new Thread(() -> THREAD.scheduleAtFixedRate(reaperActionJvmMetrics, sleepTime, sleepTime)).start();
            // Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
            System.out.println("        Started the reaper agent in the target jvm : " + pid);
            new NetworkSocketInvoker().writeAndReadFromSocket();
        };
        THREAD.schedule(startupAction, Constant.SLEEP_TIME);*/
    }

    /**
     * All kinds of opportunity here. This method only gets called if the agent is started as a parameter on the
     * start command. It does not re-define classes that are already loaded, or that are loaded out side of the classloader
     * of the agent, which is the system class loader.
     */
    @SuppressWarnings({"WeakerAccess", "UnusedParameters"})
    public byte[] transform(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classBytes)
            throws IllegalClassFormatException {
        System.out.println("        Transform : " + loader + ":" + className);
        // Return the original bytes for the class
        return classBytes;
    }

}