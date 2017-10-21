package com.pxs.reaper.action;

import com.pxs.reaper.model.*;

import java.lang.management.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

abstract class ReaperActionMBeanMetrics extends TimerTask implements ReaperAction {

    void misc(final JMetrics jMetrics, final RuntimeMXBean runtimeMXBean) {
        try {
            String vmName = runtimeMXBean.getName();
            jMetrics.setPid(vmName);
            jMetrics.setDate(new Date());
            jMetrics.setInetAddress(InetAddress.getLocalHost());
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    void threading(final JMetrics jMetrics, final ThreadMXBean threadMXBean) {
        Threading threading = Threading.builder().build();

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

    void memoryPool(final JMetrics jMetrics, final List<MemoryPoolMXBean> memoryPoolMXBeans) {
        List<MemoryPool> memoryPools = new ArrayList<>();
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

    void memory(final JMetrics jMetrics, final MemoryMXBean memoryMXBean) {
        Memory memory = Memory.builder().build();

        memory.setNonHeapMemoryUsage(memoryMXBean.getHeapMemoryUsage());
        memory.setNonHeapMemoryUsage(memoryMXBean.getNonHeapMemoryUsage());
        memory.setObjectPendingFinalizationCount(memoryMXBean.getObjectPendingFinalizationCount());
        jMetrics.setMemory(memory);
    }

    void garbageCollection(final JMetrics jMetrics, final List<GarbageCollectorMXBean> garbageCollectorMXBeans) {
        List<GarbageCollection> garbageCollections = new ArrayList<>();
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            GarbageCollection garbageCollection = GarbageCollection.builder().build();
            garbageCollection.setName(garbageCollectorMXBean.getName());
            garbageCollection.setCollectionCount(garbageCollectorMXBean.getCollectionCount());
            garbageCollection.setCollectionTime(garbageCollectorMXBean.getCollectionTime());
            garbageCollections.add(garbageCollection);
        }
        jMetrics.setGarbageCollection(garbageCollections.toArray(new GarbageCollection[garbageCollections.size()]));
    }

    void compilation(final JMetrics jMetrics, final CompilationMXBean compilationMXBean) {
        Compilation compilation = Compilation.builder().build();
        compilation.setCompilationTime(compilationMXBean.getTotalCompilationTime());
        jMetrics.setCompilation(compilation);
    }

    void classloading(final JMetrics jMetrics, final ClassLoadingMXBean classLoadingMXBean) {
        Classloading classloading = Classloading.builder().build();
        classloading.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        classloading.setTotalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount());
        classloading.setTotalLoadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        jMetrics.setClassLoading(classloading);
    }

}
