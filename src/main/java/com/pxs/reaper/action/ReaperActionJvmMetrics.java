package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
class ReaperActionJvmMetrics extends ReaperActionMBeanMetrics {

    private Transport transport;

    ReaperActionJvmMetrics() {
        transport = new WebSocketTransport();
    }

    @Override
    public void run() {
        JMetrics jMetrics = JMetrics.builder().build();

        misc(jMetrics, ManagementFactory.getRuntimeMXBean());
        threading(jMetrics, ManagementFactory.getThreadMXBean());
        memoryPool(jMetrics, ManagementFactory.getMemoryPoolMXBeans());
        memory(jMetrics, ManagementFactory.getMemoryMXBean());
        garbageCollection(jMetrics, ManagementFactory.getGarbageCollectorMXBeans());
        compilation(jMetrics, ManagementFactory.getCompilationMXBean());
        classloading(jMetrics, ManagementFactory.getClassLoadingMXBean());

        transport.postMetrics(jMetrics);
    }

    @Override
    public boolean terminate() {
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }

}