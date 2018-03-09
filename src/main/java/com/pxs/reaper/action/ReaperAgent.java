package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import org.apache.commons.lang.StringUtils;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Timer;
import java.util.TimerTask;
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
        String[] arguments = StringUtils.split(args, ";|");
        for (final String argument : arguments) {
            String[] argumentAndValue = StringUtils.split(argument, '=');
            System.setProperty(argumentAndValue[0], argumentAndValue[1]);
            log.warning("Set system property from args : " + argumentAndValue[0] + "=" + argumentAndValue[1]);
        }
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // Constant.PROPERTIES_INJECTOR.injectProperties(Constant.TRANSPORT);
                // Constant.PROPERTIES_INJECTOR.injectProperties(Constant.EXTERNAL_CONSTANTS);
                log.warning("Starting the reaper in the target jvm :");

                int sleepTime = Constant.EXTERNAL_CONSTANTS.getSleepTime();
                ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
                Constant.TIMER.scheduleAtFixedRate(reaperActionJvmMetrics, sleepTime, sleepTime);
                Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJvmMetrics::terminate));
                log.warning("Started the reaper in the target jvm :");
            }
        };
        new Timer(Boolean.TRUE).schedule(timerTask, 15000);
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