package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.toolkit.THREAD;
import org.apache.commons.lang.StringUtils;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static Logger LOG = Logger.getLogger(ReaperAgent.class.getSimpleName());

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
        properties(args);
        premain(args, instrumentation);
    }

    private static void properties(final String args) {
        // If the properties are set(i.e. only on Windows) then set these first
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
            LOG.log(Level.INFO, "Set system property from args : {0}", new Object[]{argumentAndValue[0] + "=" + argumentAndValue[1]});
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
            LOG.log(Level.SEVERE, "Agent already running : " + pid);
            // Do not start the actions running again in this JVM
            return;
        }
        LOG.log(Level.SEVERE, "Agent not running, starting... : " + pid);
        Constant.AGENT_RUNNING.set(Boolean.TRUE);
        Action timerTask = () -> {
            LOG.warning("Starting the reaper agent in the target jvm : " + pid);
            int sleepTime = Constant.EXTERNAL_CONSTANTS.getSleepTime();
            ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
            THREAD.scheduleAtFixedRate(reaperActionJvmMetrics, sleepTime, sleepTime);
            Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
            LOG.warning("Reaper agent successfully started in the target jvm : " + pid);
        };
        THREAD.scheduleAtFixedRate(timerTask, Constant.SLEEP_TIME, Constant.SLEEP_TIME);
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