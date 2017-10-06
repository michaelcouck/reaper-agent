package com.pxs.reaper.action;

import com.pxs.reaper.model.Metrics;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

    public ReaperActionJMXMetrics() {
        if (mbeanConn == null) {
            getJMXConnection();
        }
    }

    @Override
    public void run() {
        if (mbeanConn == null) {
            return;
        }
        Set<ObjectName> beanSet;
        metrics.setAttributes(new HashMap<>());
        try {
            beanSet = mbeanConn.queryNames(null, null);
            for (final ObjectName objectName : beanSet) {
                log.info("Object name : {}", objectName);
                ObjectInstance objectInstance = mbeanConn.getObjectInstance(objectName);
                MBeanInfo mBeanInfo = mbeanConn.getMBeanInfo(objectInstance.getObjectName());
                MBeanAttributeInfo[] mBeanAttributeInfos = mBeanInfo.getAttributes();
                for (final MBeanAttributeInfo mBeanAttributeInfo : mBeanAttributeInfos) {
                    if (operationSupported(objectName, mBeanAttributeInfo)) {
                        String attributeName = mBeanAttributeInfo.getName();
                        Object attributeValue = mbeanConn.getAttribute(objectName, mBeanAttributeInfo.getName());
                        if (attributeValue == null) {
                            continue;
                        }
                        log.info("            : name : {}, value : {}", attributeName, attributeValue);
                        if (CompositeDataSupport.class.isAssignableFrom(attributeValue.getClass())) {
                            CompositeDataSupport compositeDataSupport = (CompositeDataSupport) attributeValue;
                            collectMBeanDataFromComposite(compositeDataSupport);
                        } else if (MemoryPoolMXBean.class.isAssignableFrom(attributeValue.getClass())) {
                            MemoryPoolMXBean memoryPoolMXBean = (MemoryPoolMXBean) attributeValue;
                            collectMemoryPoolMXBeanMetrics(metrics, memoryPoolMXBean);
                        }
                        /*metrics.getAttributes().put(mBeanAttributeInfo.getName(),
                                mbeanConn.getAttribute(objectName, mBeanAttributeInfo.getName()));*/
                    }
                }
            }
        } catch (final IOException | InstanceNotFoundException | ReflectionException | IntrospectionException |
                AttributeNotFoundException | MBeanException e) {
            log.error("Exception accessing the MBeans in the JVM, no metrics delivered for the JVM then : ", e);
        }
    }

    private void collectMemoryPoolMXBeanMetrics(final Metrics metrics, final MemoryPoolMXBean memoryPoolMXBean) {
        Map<String, Long> memoryPoolMetrics = new HashMap<>();
        MemoryUsage memoryUsage = memoryPoolMXBean.getPeakUsage();
        memoryPoolMetrics.put("used", memoryUsage.getUsed());
        memoryPoolMetrics.put("max", memoryUsage.getMax());
        memoryPoolMetrics.put("init", memoryUsage.getInit());
        memoryPoolMetrics.put("committed", memoryUsage.getCommitted());
        metrics.getAttributes().put(memoryPoolMXBean.getName(), memoryPoolMetrics);
    }

    private void collectMBeanDataFromComposite(final CompositeDataSupport compositeDataSupport) {
        for (final Object value : compositeDataSupport.values()) {
            if (value == null) {
                continue;
            }
            if (CompositeDataSupport.class.isAssignableFrom(value.getClass())) {
                collectMBeanDataFromComposite((CompositeDataSupport) value);
            } else if (CompositeType.class.isAssignableFrom(value.getClass())) {
                CompositeType compositeType = (CompositeType) value;
                for (final String key : compositeType.keySet()) {
                    log.info("Key : " + key + ", " + compositeType.getType(key));
                }
            } else {
                log.info("Value : " + value);
            }
        }
    }

    private boolean operationSupported(final ObjectName objectName, final MBeanAttributeInfo mBeanAttributeInfo)
            throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        String supported = "true";
        if (mBeanAttributeInfo.getDescription().startsWith("CollectionUsageThreshold")) {
            supported = mbeanConn.getAttribute(objectName, "CollectionUsageThresholdSupported").toString();
        } else if (mBeanAttributeInfo.getDescription().startsWith("UsageThreshold")) {
            supported = mbeanConn.getAttribute(objectName, "UsageThresholdSupported").toString();
        }
        return Boolean.valueOf(supported);
    }

    private void getJMXConnection() {
        if (retryInterval != 0 && System.currentTimeMillis() - retryInterval < RETRY_DELAY) {
            // Don't continuously ping the JVM, wait a minute between each attempt
            return;
        }
        retryInterval = System.currentTimeMillis();

        // We only go to the local host on port 8500
        JMXServiceURL serviceUrl;
        String url = "service:jmx:rmi:///jndi/rmi://localhost:8500/jmxrmi";
        try {
            serviceUrl = new JMXServiceURL(url);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
            mbeanConn = jmxConnector.getMBeanServerConnection();
        } catch (final Exception e) {
            log.warn("Couldn't connect to localhost JMX, no metrics delivered for the JVM : ", e);
        }
    }
}