package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.Reaper;
import com.pxs.reaper.agent.toolkit.ChildFirstClassLoader;
import com.pxs.reaper.agent.toolkit.MANIFEST;
import com.pxs.reaper.agent.toolkit.THREAD;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

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
        premain(args, instrumentation);
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
            URL[] urls = MANIFEST.getClassPathUrls();
            if (!Thread.currentThread().getContextClassLoader().getClass().isAssignableFrom(ChildFirstClassLoader.class)) {
                URLClassLoader urlClassLoader = new ChildFirstClassLoader(urls);
                Thread.currentThread().setContextClassLoader(urlClassLoader);
            }
            System.out.println("Starting the reaper agent in the target jvm : " + pid);
            int sleepTime = (int) Constant.SLEEP_TIME;
            ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
            THREAD.scheduleAtFixedRate(reaperActionJvmMetrics, sleepTime, sleepTime);
            Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
            System.out.println("Reaper agent successfully started in the target jvm : " + pid);
        };
        THREAD.schedule(timerTask, Constant.SLEEP_TIME);
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