package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import org.apache.commons.lang.StringUtils;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.TimerTask;
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

    private static Logger log = Logger.getLogger(ReaperAgent.class.getSimpleName());

    /**
     * TODO: Log bug report for linux. Passing arguments does not work on linux, string concatenation logical error in the code.
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
        // TODO: Check if there is already an agent present
        // TODO: Dynamically reload the properties if they have changed
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
            log.log(Level.INFO, "Set system property from args : {0}", new Object[]{argumentAndValue[0] + "=" + argumentAndValue[1]});
        }
    }

    /**
     * These instructions tell the JVM to call this method when loading class files.
     *
     * @param args            a set of arguments that the JVM will call the method with
     * @param instrumentation the instrumentation implementation of the JVM
     */
    public static void premain(final String args, final Instrumentation instrumentation) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                log.warning("Starting the reaper in the target jvm :");

                int sleepTime = Constant.EXTERNAL_CONSTANTS.getSleepTime();
                ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
                Constant.TIMER.scheduleAtFixedRate(reaperActionJvmMetrics, sleepTime, sleepTime);
                // new Timer(true).scheduleAtFixedRate(reaperActionJvmMetrics, sleepTime, sleepTime);
                Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
                log.warning("Started the reaper in the target jvm :");
            }
        };
        Constant.TIMER.schedule(timerTask, 15000);
        // new Timer(Boolean.TRUE).schedule(timerTask, 15000);
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