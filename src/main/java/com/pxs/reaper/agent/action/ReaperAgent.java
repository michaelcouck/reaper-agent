package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.Reaper;
import com.pxs.reaper.agent.action.instrumentation.SocketClassFileTransformer;
import com.pxs.reaper.agent.toolkit.ChildFirstClassLoader;
import com.pxs.reaper.agent.toolkit.MANIFEST;
import com.pxs.reaper.agent.toolkit.THREAD;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
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
     * TODO: But works on windows... Sad...
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
        if (!shouldAttach()) {
            return;
        }
        if (!shouldAttachToProcess()) {
            return;
        }

        System.out.println("        Pre main : ");
        retransformClasses(instrumentation);
        startHeartbeatThread(ManagementFactory.getRuntimeMXBean().getName());
    }

    /**
     * Checks to see if the agent is already attached and running, if it is then
     * we should not attach again.
     *
     * @return whether we should attach or not
     */
    private static boolean shouldAttach() {
        synchronized (LOADED) {
            if (LOADED.get()) {
                System.out.println("        Pre-main already called : ");
                return !LOADED.get();
            }
            LOADED.set(Boolean.TRUE);
            return LOADED.get();
        }
    }

    /**
     * Avoids attaching to Intellij and potentially other processes that we don't want to attach to.
     *
     * @return whether we should attach to this process or not
     */
    private static boolean shouldAttachToProcess() {
        String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
        String bootClassPath = ManagementFactory.getRuntimeMXBean().getBootClassPath();
        /*System.out.println("        Class path : " + classPath);
        System.out.println("        Boot class path : " + bootClassPath);*/
        String ideaUi = "idea-IU";
        if (classPath.contains(ideaUi) || bootClassPath.contains(ideaUi)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private static void retransformClasses(final Instrumentation instrumentation) {
        try {
            // Add our own classes to the boot class loader
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(MANIFEST.getPathToAgent()));
            instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(MANIFEST.getPathToAgent()));
            // Do any transformation that we need
            instrumentation.addTransformer(new SocketClassFileTransformer(), true);
            instrumentation.retransformClasses(Socket.class, SocketImpl.class);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts the thread that will periodically post metrics to the service for analysis.
     *
     * @param pid the pid of this process for logging
     */
    @SuppressWarnings("InfiniteLoopStatement")
    private static void startHeartbeatThread(final String pid) {
        new Thread(() -> {
            System.out.println("        Starting the reaper agent in the target jvm : " + pid);
            URL[] urls = MANIFEST.getClassPathUrls();
            System.out.println("        URLs for class loader : " + urls.length);
            ChildFirstClassLoader childFirstClassLoader = new ChildFirstClassLoader(urls);
            Thread.currentThread().setContextClassLoader(childFirstClassLoader);
            ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
            Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
            System.out.println("        Started the reaper agent in the target jvm : " + pid);
            while (true) {
                try {
                    Thread.sleep(Constant.SLEEP_TIME);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                reaperActionJvmMetrics.run();
            }
        }).start();
    }

    /**
     * Note to self: Class loading protection domain and linkage errors for some strange reason.
     *
     * @param pid the pid of the process, just for logging
     */
    @SuppressWarnings("unused")
    private static void startExecutorThread(final String pid) {
        Action startupAction = () -> {
            URL[] urls = MANIFEST.getClassPathUrls();
            URLClassLoader urlClassLoader = new ChildFirstClassLoader(urls);
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            System.out.println("        Starting the reaper agent in the target jvm : " + pid);
            ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
            THREAD.scheduleAtFixedRate(reaperActionJvmMetrics, Constant.SLEEP_TIME, Constant.SLEEP_TIME);
            Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
            System.out.println("        Started the reaper agent in the target jvm : " + pid);
        };
        THREAD.schedule(startupAction, Constant.SLEEP_TIME);
    }

    /**
     * Alternative entry point for re-definition in stead of {@link Instrumentation#retransformClasses(Class[])}.
     *
     * @param instrumentation the JVM entry point for redefining classes
     * @throws IOException                bla.
     * @throws UnmodifiableClassException bla.
     * @throws ClassNotFoundException     bla.
     */
    @SuppressWarnings("unused")
    private static void redefineClasses(final Instrumentation instrumentation) throws IOException, UnmodifiableClassException, ClassNotFoundException {
        Class[] classesToRedefine = new Class[]{Socket.class, SocketImpl.class};
        for (final Class<?> clazz : classesToRedefine) {
            instrumentation.redefineClasses(getClassDefinition(clazz));
        }
    }

    private static ClassDefinition getClassDefinition(final Class<?> clazz) throws IOException {
        String className = clazz.getName();
        String classAsPath = className.replace('.', '/') + ".class";
        InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(classAsPath);
        byte[] classFileBytes = IOUtils.toByteArray(stream);
        return new ClassDefinition(clazz, classFileBytes);
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
        // Return the original bytes for the class for now... We dynamically attach, so this method is not called.
        return classBytes;
    }

}