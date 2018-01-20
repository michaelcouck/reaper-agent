package com.pxs.reaper.action;

import com.pxs.reaper.Constant;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

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

    /**
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
    public static void agentmain(String args, Instrumentation instrumentation) throws Exception {
        premain(args, instrumentation);
    }

    /**
     * These instructions tell the JVM to call this method when loading class files.
     *
     * @param args            a set of arguments that the JVM will call the method with
     * @param instrumentation the instrumentation implementation of the JVM
     */
    public static void premain(final String args, final Instrumentation instrumentation) {
        String reaperMicroservice = "reaper-microservice";
        String classpath = System.getProperty("java.class.path", "");
        System.out.println("Reaper microservice on class path : " + classpath.indexOf(reaperMicroservice));
        if (classpath.contains(reaperMicroservice)) {
            System.out.println("Shutting down, don't monitor the micro service : " + classpath);
            return;
        }
        Constant.PROPERTIES_INJECTOR.injectProperties(Constant.EXTERNAL_CONSTANTS);
        int sleepTime = Constant.EXTERNAL_CONSTANTS.getSleepTime();
        ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
        Constant.TIMER.scheduleAtFixedRate(reaperActionJvmMetrics, sleepTime, sleepTime);
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
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
        return classBytes;
    }

}