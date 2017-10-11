package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.Transport;
import com.pxs.reaper.WebSocketTransport;
import com.pxs.reaper.model.Metrics;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.props.annotations.Property;

import java.lang.management.*;
import java.util.List;
import java.util.TimerTask;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
@Slf4j
@Setter
public class ReaperActionJvmMetrics extends TimerTask implements ReaperAction {

    @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
    private int sleepTime;

    private Transport transport;

    public ReaperActionJvmMetrics() {
        transport = new WebSocketTransport();

        Constant.PROPERTIES_INJECTOR.injectProperties(this);
        Constant.TIMER.scheduleAtFixedRate(this, sleepTime, sleepTime);
    }

    @Override
    public void run() {
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        classLoadingMXBean.getLoadedClassCount();
        classLoadingMXBean.getTotalLoadedClassCount();
        classLoadingMXBean.getUnloadedClassCount();

        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        compilationMXBean.getTotalCompilationTime();

        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            garbageCollectorMXBean.getCollectionCount();
            garbageCollectorMXBean.getCollectionTime();
        }

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        memoryMXBean.getHeapMemoryUsage();
        memoryMXBean.getNonHeapMemoryUsage();
        memoryMXBean.getObjectPendingFinalizationCount();

        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (final MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            memoryPoolMXBean.getName();

            memoryPoolMXBean.getCollectionUsage();
            if (memoryPoolMXBean.isCollectionUsageThresholdSupported()) {
                memoryPoolMXBean.getCollectionUsageThreshold();
                memoryPoolMXBean.getCollectionUsageThresholdCount();
            }
            memoryPoolMXBean.getPeakUsage();
            memoryPoolMXBean.getType();
            memoryPoolMXBean.getUsage();
            if (memoryPoolMXBean.isUsageThresholdSupported()) {
                memoryPoolMXBean.getUsageThreshold();
                memoryPoolMXBean.getUsageThresholdCount();
            }
        }

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.findDeadlockedThreads();
        threadMXBean.findMonitorDeadlockedThreads();
        long[] threadIds = threadMXBean.getAllThreadIds();
        threadMXBean.getCurrentThreadCpuTime();
        threadMXBean.getCurrentThreadUserTime();
        threadMXBean.getPeakThreadCount();
        threadMXBean.getDaemonThreadCount();
        threadMXBean.getThreadCount();

        threadMXBean.getThreadCpuTime(threadIds[0]);
        threadMXBean.getThreadInfo(threadIds[0]);
        threadMXBean.getTotalStartedThreadCount();

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        operatingSystemMXBean.getArch();
        operatingSystemMXBean.getAvailableProcessors();
        operatingSystemMXBean.getSystemLoadAverage();

        Metrics metrics = Metrics.builder().build();

        transport.postMetrics(metrics);
    }

    @Override
    public void terminate() {
        this.cancel();
        Constant.TIMER.purge();
    }

}