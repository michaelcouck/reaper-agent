package com.pxs.reaper.action;

import com.pxs.reaper.model.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.*;
import java.util.Set;

/**
 * ThreadMXBean: threadMXBean
 * ClassLoadingMXBean: classLoadingMXBean
 * BufferPoolMXBean: mapped, bufferPoolMXBean
 * GarbageCollectorMXBean: psScavenge, psMarkSweep
 * MemoryPoolMXBean: metaspace, psOldGen, psEdenSpace, codeCache, compressedClassSpace, psSurvivorSpace
 * <p>
 * Not interesting:
 * LoggingMXBean: loggingMXBean
 * RuntimeMXBean: runtimeMXBean
 * DiagnosticCommandMBean diagnosticCommandMBean
 * OperatingSystemMXBean: operatingSystemMXBean - collected by Sigar
 * HotSpotDiagnosticMXBean: hotSpotDiagnosticMXBean
 * <p>
 * <p>
 * Parameters to allow JMX access.
 * <p>
 * -Dcom.sun.management.jmxremote=false
 * -Dcom.sun.management.jmxremote.local.only=true
 * -Dcom.sun.management.jmxremote.authenticate=false
 * -Dcom.sun.management.jmxremote.ssl=false
 * -Djava.rmi.server.hostname=localhost
 * -Dcom.sun.management.jmxremote.port=8500
 * -Dcom.sun.management.jmxremote.rmi.port=8501
 */
@Slf4j
@Setter
public class ReaperActionJMXMetrics implements ReaperAction, Runnable {

    private static final long RETRY_DELAY = 600000;

    private volatile Metrics metrics;
    private MBeanServerConnection mbeanConn;
    private long retryInterval = 0;

    @Override
    @SuppressWarnings("unused")
    public void run() {
        if (mbeanConn == null) {
            getJMXConnection();
            if (mbeanConn == null) {
                return;
            }
        }
        Set<ObjectName> beanSet;
        try {
            beanSet = mbeanConn.queryNames(null, null);
            for (final ObjectName objectName : beanSet) {
                System.out.println("Object name : " + objectName);
                ObjectInstance objectInstance = mbeanConn.getObjectInstance(objectName);

                MBeanInfo mBeanInfo = mbeanConn.getMBeanInfo(objectInstance.getObjectName());
                System.out.println("        : " + mBeanInfo.getDescription());

                MBeanAttributeInfo[] mBeanAttributeInfos = mBeanInfo.getAttributes();
                for (final MBeanAttributeInfo mBeanAttributeInfo : mBeanAttributeInfos) {
                    System.out.println("            : " + mBeanAttributeInfo.toString());
                }
                MBeanOperationInfo[] mBeanOperationInfos = mBeanInfo.getOperations();
                for (final MBeanOperationInfo mBeanOperationInfo : mBeanOperationInfos) {
                    // System.out.println(mBeanOperationInfo.toString());
                }

                // mbeanConn.invoke(objectName, "", null, null);
                // getThreadMXBeanMetrics(metrics, (ThreadMXBean) objectInstance);
                // getMemoryPoolMXBeanMetrics(objectName.toString(), metrics, (MemoryPoolMXBean) objectInstance);
            }
        } catch (final IOException | InstanceNotFoundException | ReflectionException e) {
            log.error("Exception accessing the MBeans in the JVM, no metrics delivered for the JVM then : ", e);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    private void getClassLoadingMXBeanMetrics(final Metrics metrics, final ClassLoadingMXBean classLoadingMXBean) {
        ClassPool classPool = ClassPool.builder()
                .loadedClassCount(classLoadingMXBean.getLoadedClassCount())
                .totalLoadedClassCount(classLoadingMXBean.getTotalLoadedClassCount())
                .unloadedClassCount(classLoadingMXBean.getUnloadedClassCount())
                .build();
        metrics.setClassPool(classPool);
    }

    private void getGarbageCollectorMXBeanMetrics(final Metrics metrics, final GarbageCollectorMXBean garbageCollectorMXBean) {
        GarbageCollection garbageCollection = GarbageCollection.builder()
                .collectionCount(garbageCollectorMXBean.getCollectionCount())
                .collectionTime(garbageCollectorMXBean.getCollectionTime())
                .build();
        metrics.setGarbageCollection(garbageCollection);
    }

    private void getThreadMXBeanMetrics(final Metrics metrics, final ThreadMXBean threadMXBean) {
        ThreadPool threadPool = ThreadPool.builder().build();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        for (final ThreadInfo threadInfo : threadInfos) {
            threadPool.setBlockedCount(threadPool.getBlockedCount() + threadInfo.getBlockedCount());
            threadPool.setBlockedTime(threadPool.getBlockedTime() + threadInfo.getBlockedTime());
            MonitorInfo[] monitorInfos = threadInfo.getLockedMonitors();
            for (final MonitorInfo monitorInfo : monitorInfos) {
                threadPool.setLockedStackDepth(threadPool.getLockedStackDepth() + monitorInfo.getLockedStackDepth());
            }
            threadPool.getThreadStates().add(threadInfo.getThreadState());
            threadPool.setWaitedCount(threadPool.getWaitedCount() + threadInfo.getWaitedCount());
            threadPool.setWaitedTime(threadPool.getWaitedTime() + threadInfo.getWaitedTime());
            if (threadInfo.isInNative()) {
                threadPool.setInNative(threadPool.getInNative() + 1);
            }
            if (threadInfo.isSuspended()) {
                threadPool.setSuspended(threadPool.getSuspended() + 1);
            }
        }
        metrics.setThreadPool(threadPool);
    }

    private void getBufferPoolMXBeanMetrics(final String name, final Metrics metrics, final BufferPoolMXBean bufferPoolMXBean) {
        BufferPool bufferPool = BufferPool.builder()
                .count(bufferPoolMXBean.getCount())
                .memoryUsed(bufferPoolMXBean.getMemoryUsed())
                .totalCapacity(bufferPoolMXBean.getTotalCapacity())
                .build();
        if (name.contains("mapped")) {
            metrics.setMapped(bufferPool);
        } else {
            metrics.setBufferPoolMXBean(bufferPool);
        }
    }

    private void getMemoryPoolMXBeanMetrics(final String name, final Metrics metrics, final MemoryPoolMXBean memoryPoolMXBean) {
        MemoryPool memoryPool = MemoryPool.builder()
                .build();

        // Ignored for now
        /*memoryPoolMXBean.getPeakUsage().getCommitted();
        memoryPoolMXBean.getCollectionUsage().getCommitted();*/
        /*memoryPoolMXBean.getCollectionUsageThreshold();
        memoryPoolMXBean.getCollectionUsageThresholdCount();*/
        /*memoryPoolMXBean.isCollectionUsageThresholdExceeded();*/
        /*memoryPoolMXBean.getType();*/

        memoryPoolMXBean.getUsage().getInit();
        memoryPoolMXBean.getUsage().getUsed();
        memoryPoolMXBean.getUsage().getMax();
        memoryPoolMXBean.getUsage().getCommitted();

        memoryPoolMXBean.getUsageThreshold();
        memoryPoolMXBean.getUsageThresholdCount();
        memoryPoolMXBean.isUsageThresholdExceeded();

        if (name.toLowerCase().contains("metaspace")) {
            metrics.setMetaspace(memoryPool);
        }
    }

    private void getJMXConnection() {
        if (retryInterval != 0 && System.currentTimeMillis() - retryInterval < RETRY_DELAY) {
            // Don't continuously ping the JVM, wait a minute between each attempt
            return;
        }
        retryInterval = System.currentTimeMillis();

        // We only go to the local host on port 8500
        String url = "service:jmx:rmi:///jndi/rmi://localhost:8500/jmxrmi";
        JMXServiceURL serviceUrl;
        try {
            serviceUrl = new JMXServiceURL(url);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
            mbeanConn = jmxConnector.getMBeanServerConnection();
        } catch (final Exception e) {
            log.warn("Couldn't connect to localhost JMX, no metrics delivered for the JVM : ", e);
        }
    }
}