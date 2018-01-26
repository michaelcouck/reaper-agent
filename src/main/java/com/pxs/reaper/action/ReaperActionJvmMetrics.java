package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.transport.Transport;

import java.lang.management.ManagementFactory;

/**
 * This class will collect all the telemetry data from the Java process, populate a {@link JMetrics} object
 * and post it to the endpoint that is defined by the implementation of {@link Transport}.
 * <p>
 * Telemetry is gathered for memory, threads, garbage collection etc. The Operating system telemetry is gathered
 * by the {@link ReaperActionOSMetrics} class so not necessary to double the load.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
public class ReaperActionJvmMetrics extends ReaperActionMetrics {

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        JMetrics jMetrics = getMetrics();
        Constant.TRANSPORT.postMetrics(jMetrics);
    }

    public JMetrics getMetrics() {
        JMetrics jMetrics = new JMetrics();

        misc(jMetrics, ManagementFactory.getRuntimeMXBean());
        threading(jMetrics, ManagementFactory.getThreadMXBean());
        memoryPool(jMetrics, ManagementFactory.getMemoryPoolMXBeans());
        memory(jMetrics, ManagementFactory.getMemoryMXBean());
        garbageCollection(jMetrics, ManagementFactory.getGarbageCollectorMXBeans());
        compilation(jMetrics, ManagementFactory.getCompilationMXBean());
        classloading(jMetrics, ManagementFactory.getClassLoadingMXBean());
        os(jMetrics, ManagementFactory.getOperatingSystemMXBean());

        return jMetrics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean terminate() {
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }

}