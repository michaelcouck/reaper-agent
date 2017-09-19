package com.pxs.reaper.action;

import com.pxs.reaper.model.Metrics;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.HashMap;
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
                log.debug("Object name : {}", objectName);
                ObjectInstance objectInstance = mbeanConn.getObjectInstance(objectName);
                MBeanInfo mBeanInfo = mbeanConn.getMBeanInfo(objectInstance.getObjectName());
                MBeanAttributeInfo[] mBeanAttributeInfos = mBeanInfo.getAttributes();
                for (final MBeanAttributeInfo mBeanAttributeInfo : mBeanAttributeInfos) {
                    if (operationSupported(objectName, mBeanAttributeInfo)) {
                        log.debug("            : {}", mBeanAttributeInfo.toString());
                        metrics.getAttributes().put(mBeanAttributeInfo.getName(),
                                mbeanConn.getAttribute(objectName, mBeanAttributeInfo.getName()));
                    }
                }
            }
        } catch (final IOException | InstanceNotFoundException | ReflectionException | IntrospectionException |
                AttributeNotFoundException | MBeanException e) {
            log.error("Exception accessing the MBeans in the JVM, no metrics delivered for the JVM then : ", e);
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