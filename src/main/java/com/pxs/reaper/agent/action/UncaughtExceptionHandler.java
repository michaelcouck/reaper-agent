package com.pxs.reaper.agent.action;

import java.util.Map;

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

    private Thread.UncaughtExceptionHandler parent;
    private ReaperActionJvmMetrics reaperActionJvmMetrics;

    public UncaughtExceptionHandler(final Thread.UncaughtExceptionHandler parent, final ReaperActionJvmMetrics reaperActionJvmMetrics) {
        this.parent = parent;
        this.reaperActionJvmMetrics = reaperActionJvmMetrics;
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        Map<String, Integer> exceptions = reaperActionJvmMetrics.getExceptions();
        String exceptionName = e.getClass().getName();
        Integer count = exceptions.get(exceptionName);
        if (count == null) {
            count = 0;
        }
        count++;
        exceptions.put(exceptionName, count);
        parent.uncaughtException(t, e);
    }

}