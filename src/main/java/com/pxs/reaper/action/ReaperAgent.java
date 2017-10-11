package com.pxs.reaper.action;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
@SuppressWarnings("WeakerAccess")
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
    @SuppressWarnings("unused")
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
        ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
    }

    /**
     * TODO: Can we remove this? We don't need it.
     */
    @SuppressWarnings("UnusedParameters")
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