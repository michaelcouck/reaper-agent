package com.pxs.reaper.agent.action;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the exception handler interceptor, it collects and counts exceptions that happen in the jvm, and the types. These
 * are posted to the controller for analysis.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 04-05-2018
 */
@SuppressWarnings("WeakerAccess")
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private Thread.UncaughtExceptionHandler parent;
    private ReaperActionJvmMetrics reaperActionJvmMetrics;

    public UncaughtExceptionHandler(final Thread.UncaughtExceptionHandler parent, final ReaperActionJvmMetrics reaperActionJvmMetrics) {
        this.parent = parent;
        this.reaperActionJvmMetrics = reaperActionJvmMetrics;
    }

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        try {
            Map<String, Integer> exceptions = reaperActionJvmMetrics.getExceptions();

            String exceptionName = e.getClass().getName();
            Integer count = exceptions.get(exceptionName);
            if (count == null) {
                count = 0;
            }
            count++;
            exceptions.put(exceptionName, count);
            logger.log(Level.SEVERE, "Exception in uncaught exception handler", e);
            if (parent != null) {
                parent.uncaughtException(t, e);
            }
        } catch (final Throwable ignore) {
            ignore.printStackTrace();
        }

    }

}