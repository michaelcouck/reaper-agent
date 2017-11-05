package com.pxs.reaper.action;

import com.pxs.reaper.model.*;
import com.pxs.reaper.toolkit.HOST;

import java.lang.management.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Base class for JMX operations on management MBeans for the JVM. Takes various MBeans from the JVM and populates
 * metrics model objects for TRANSPORT to the micro service for analysis and model building.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 22-10-2017
 */
abstract class ReaperActionMBeanMetrics extends TimerTask implements ReaperAction {

    /**
     * Populates the miscellaneous items for TRANSPORT, the ip address, timestamp etc.
     *
     * @param jMetrics      the JVM metrics model object for TRANSPORT
     * @param runtimeMXBean the runtime MBean for the name of the JVM
     */
    void misc(final JMetrics jMetrics, final RuntimeMXBean runtimeMXBean) {
        try {
            String vmName = runtimeMXBean.getName();
            jMetrics.setPid(vmName);
            jMetrics.setDate(new Date());
            InetAddress inetAddress = InetAddress.getByName(HOST.hostname());
            jMetrics.setInetAddress(inetAddress);
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Populates the thread items for TRANSPORT, how many threads, the core times, deadlocked, etc.
     *
     * @param jMetrics     the JVM metrics model object for TRANSPORT
     * @param threadMXBean the thread MBean for access to the threads in the JVM
     */
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
        for (int i = 0; i < threadInfos.length; i++) {
            threadInfos[i] = threadMXBean.getThreadInfo(threadIds[i]);
            threadCpuTimes[i] = threadMXBean.getThreadCpuTime(threadIds[i]);
        }
        threading.setThreadInfos(threadInfos);
        threading.setThreadCpuTimes(threadCpuTimes);

        jMetrics.setThreading(threading);
    }

    /**
     * Populates the memory pool model objects.
     *
     * @param jMetrics          the JVM metrics model object for TRANSPORT
     * @param memoryPoolMXBeans the memory pool beans from the JVM
     */
    void memoryPool(final JMetrics jMetrics, final List<MemoryPoolMXBean> memoryPoolMXBeans) {
        List<MemoryPool> memoryPools = new ArrayList<>();
        if (jMetrics.getMemoryPools() != null) {
            memoryPools.addAll(Arrays.asList(jMetrics.getMemoryPools()));
        }
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

    /**
     * Populates the memory objects for TRANSPORT, the heap and non heap usage.
     *
     * @param jMetrics     the JVM metrics model object for TRANSPORT
     * @param memoryMXBean the memory bean from the JVM
     */
    void memory(final JMetrics jMetrics, final MemoryMXBean memoryMXBean) {
        Memory memory = Memory.builder().build();

        memory.setNonHeapMemoryUsage(memoryMXBean.getHeapMemoryUsage());
        memory.setNonHeapMemoryUsage(memoryMXBean.getNonHeapMemoryUsage());
        memory.setObjectPendingFinalizationCount(memoryMXBean.getObjectPendingFinalizationCount());
        jMetrics.setMemory(memory);
    }

    /**
     * Populates the garbage collection beans for TRANSPORT. How many collections, the time taken for each collection etc.
     *
     * @param jMetrics                the JVM metrics model object for TRANSPORT
     * @param garbageCollectorMXBeans the garbage collection beans for the JVM
     */
    void garbageCollection(final JMetrics jMetrics, final List<GarbageCollectorMXBean> garbageCollectorMXBeans) {
        List<GarbageCollection> garbageCollections = new ArrayList<>();
        if (jMetrics.getGarbageCollection() != null) {
            garbageCollections.addAll(Arrays.asList(jMetrics.getGarbageCollection()));
        }
        for (final GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
            GarbageCollection garbageCollection = GarbageCollection.builder().build();
            garbageCollection.setName(garbageCollectorMXBean.getName());
            garbageCollection.setCollectionCount(garbageCollectorMXBean.getCollectionCount());
            garbageCollection.setCollectionTime(garbageCollectorMXBean.getCollectionTime());
            garbageCollections.add(garbageCollection);
        }
        jMetrics.setGarbageCollection(garbageCollections.toArray(new GarbageCollection[garbageCollections.size()]));
    }

    /**
     * Compilation times, probably not interesting. Could be in environments where there is a lot of instrumentation.
     *
     * @param jMetrics          the JVM metrics model object for TRANSPORT
     * @param compilationMXBean the compilation bean for the JVM
     */
    void compilation(final JMetrics jMetrics, final CompilationMXBean compilationMXBean) {
        Compilation compilation = Compilation.builder().build();
        compilation.setCompilationTime(compilationMXBean.getTotalCompilationTime());
        jMetrics.setCompilation(compilation);
    }

    /**
     * Classloading metrics for the JVM, could be interesting, specially is if there is a class memory leak, and indeed because
     * classes are loaded and stored off heap, it could potentially crash the machine that they are on.
     *
     * @param jMetrics           the JVM metrics model object for TRANSPORT
     * @param classLoadingMXBean the class loading bean for the JVM
     */
    void classloading(final JMetrics jMetrics, final ClassLoadingMXBean classLoadingMXBean) {
        Classloading classloading = Classloading.builder().build();
        classloading.setLoadedClassCount(classLoadingMXBean.getLoadedClassCount());
        classloading.setTotalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount());
        classloading.setTotalLoadedClassCount(classLoadingMXBean.getUnloadedClassCount());
        jMetrics.setClassLoading(classloading);
    }

}
