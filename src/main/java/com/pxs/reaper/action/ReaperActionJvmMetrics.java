package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.*;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

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
class ReaperActionJvmMetrics extends TimerTask implements ReaperAction {

    private Transport transport;

    ReaperActionJvmMetrics() {
        transport = new WebSocketTransport();
    }

    @Override
    public void run() {
        JMetrics jMetrics = JMetrics.builder().build();

        misc(jMetrics);
        classloading(jMetrics);
        compilation(jMetrics);
        garbageCollection(jMetrics);
        memory(jMetrics);
        memoryPool(jMetrics);
        threading(jMetrics);

        transport.postMetrics(jMetrics);
    }

    private void misc(final JMetrics jMetrics) {
        try {
            String vmName = ManagementFactory.getRuntimeMXBean().getName();
            jMetrics.setPid(vmName);
            jMetrics.setDate(new Date());
            jMetrics.setInetAddress(InetAddress.getLocalHost());
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void threading(final JMetrics jMetrics) {
        Threading threading = Threading.builder().build();

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threading.setDeadLockedThreads(threadMXBean.findDeadlockedThreads());
        threading.setMonitorDeadLockedThreads(threadMXBean.findMonitorDeadlockedThreads());
        threading.setPeakThreadCount(threadMXBean.getPeakThreadCount());
        threading.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        threading.setThreadCount(threadMXBean.getThreadCount());
        threading.setTotalStartedThreadCount(threadMXBean.getTotalStartedThreadCount());

        long[] threadIds = threadMXBean.getAllThreadIds();
        threading.setThreadIds(threadIds);

        long[] threadCpuTimes = new long[threadIds.length];
        ThreadInfo[] threadInfos = new ThreadInfo[threadIds.length];

        int index = 0;
        for (final long threadId : threadIds) {
            threadCpuTimes[index] = threadMXBean.getThreadCpuTime(threadId);
            threadInfos[index] = threadMXBean.getThreadInfo(threadId);
            index++;
        }

        threading.setThreadCpuTimes(threadCpuTimes);
        threading.setThreadInfos(threadInfos);

        jMetrics.setThreading(threading);
    }

    private void memoryPool(final JMetrics jMetrics) {
        List<MemoryPool> memoryPools = new ArrayList<>();
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (final MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            MemoryPool memoryPool = MemoryPool.builder().build();
            memoryPool.setName(memoryPoolMXBean.getName());

            memoryPool.setCollectionUsage(memoryPoolMXBean.getCollectionUsage());
            if (memoryPoolMXBean.isCollectionUsageThresholdSupported()) {
                memoryPool.setCollectionUsageThreshold(memoryPoolMXBean.getCollectionUsageThreshold());
                memoryPool.setCollectionUsageThresholdCount(memoryPoolMXBean.getCollectionUsageThresholdCount());
            }
            memoryPool.setPeakUsage(memoryPoolMXBean.getPeakUsage());
            memoryPool.setType(memoryPoolMXBean.getType());
            memoryPool.setUsage(memoryPoolMXBean.getUsage());
            if (memoryPoolMXBean.isUsageThresholdSupported()) {
                memoryPool.setUsageThreshold(memoryPoolMXBean.getUsageThreshold());
                memoryPool.setUsageThresholdCount(memoryPoolMXBean.getUsageThresholdCount());
            }
            memoryPools.add(memoryPool);
        }
        jMetrics.setMemoryPools(memoryPools.toArray(new MemoryPool[memoryPools.size()]));
    }

    private void memory(final JMetrics jMetrics) {
        Memory memory = Memory.builder().build();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        memory.setNonHeapMemoryUsage(memoryMXBean.getHeapMemoryUsage());
        memory.setNonHeapMemoryUsage(memoryMXBean.getNonHeapMemoryUsage());
        memory.setObjectPendingFinalizationCount(memoryMXBean.getObjectPendingFinalizationCount());
        jMetrics.setMemory(memory);
    }

    private void garbageCollection(final JMetrics jMetrics) {
        List<GarbageCollection> garbageCollections = new ArrayList<>();
        List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            GarbageCollection garbageCollection = GarbageCollection.builder().build();
            garbageCollection.setName(garbageCollectorMXBean.getName());
            garbageCollection.setCollectionCount(garbageCollectorMXBean.getCollectionCount());
            garbageCollection.setCollectionTime(garbageCollectorMXBean.getCollectionTime());
            garbageCollections.add(garbageCollection);
        }
        jMetrics.setGarbageCollection(garbageCollections.toArray(new GarbageCollection[garbageCollections.size()]));
    }

    private void compilation(final JMetrics jMetrics) {
        Compilation compilation = Compilation.builder().build();
        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        compilation.setCompilationTime(compilationMXBean.getTotalCompilationTime());
        jMetrics.setCompilation(compilation);
    }

    private void classloading(final JMetrics jMetrics) {
        Classloading classloading = Classloading.builder().build();
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        classloading.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        classloading.setTotalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount());
        classloading.setTotalLoadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        jMetrics.setClassLoading(classloading);
    }

    @Override
    public boolean terminate() {
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }

}