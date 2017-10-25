package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import ikube.toolkit.THREAD;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.props.annotations.Property;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * The pid for the Java processes are often not exposed, for example on OpenShift, consequently it is not possible to dynamically attach a n agent
 * to the process. This class adapts the JMX beans from the local host, proxies them to conform to management MBeans, and invokes get operations to
 * extract telemetry and metrics information from the JVM.
 * <p>
 * <p>
 * Parameters to allow JMX access.
 * <pre>
 *      -Djava.rmi.server.hostname=localhost
 *      -Dcom.sun.management.jmxremote.local.only=true
 *      -Dcom.sun.management.jmxremote.rmi.port=8501
 *
 *      Automatically done by OpenShift
 *      -Dcom.sun.management.jmxremote=false *
 *      -Dcom.sun.management.jmxremote.ssl=false *
 *      -Dcom.sun.management.jmxremote.port=1099 *
 *      -Dcom.sun.management.jmxremote.authenticate=false *
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-10-2017
 */
@Slf4j
@Setter
@SuppressWarnings("WeakerAccess")
public class ReaperActionJmxMetrics extends ReaperActionMBeanMetrics {

    /**
     * The uri to post the metrics to
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "localhost-jmx-uri")
    private String reaperJmxUri = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";

    /**
     * Keep retrying every 15 minutes for localhost JMX connections.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private long delay = 1000 * 60 * 15;
    /**
     * Transport to the service for accumulation of telemetry data for analysis.
     */
    private Transport transport;
    /**
     * The JMX connector to the Java process, instance variable so as to release resources on terminate
     */
    private JMXConnector jmxConnector;
    /**
     * The management bean connection to the JMX MBeans, instance variable so as to release resources on terminate
     */
    private MBeanServerConnection mbeanConn;

    public ReaperActionJmxMetrics() {
        transport = new WebSocketTransport();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        run(3, delay);
    }

    /**
     * Connects to the local JMX management beans. Iterates through all the beans, extracting interesting metrics and telemetry data
     * from the beans. Populates a {@link JMetrics} object that is converted to json for transport the central analyzer.
     *
     * @param retry the number of times to retry connecting to the JMX MBeans
     * @param delay the delay to apply. The delay increases with each retry
     */
    private void run(final int retry, final long delay) {
        Set<ObjectName> objectNames;
        try {
            getMBeanServerConnection();
            objectNames = mbeanConn.queryNames(null, null);
        } catch (final Exception e) {
            if (retry > 0) {
                long sleep = delay / retry;
                THREAD.sleep(sleep);
                run(retry - 1, delay);
                return;
            } else {
                log.debug("No connection to JMX : ", e);
                return;
            }
        }

        JMetrics jMetrics = JMetrics.builder().build();
        for (final ObjectName objectName : objectNames) {
            try {
                MBeanInfo mBeanInfo = mbeanConn.getMBeanInfo(objectName);
                String className = mBeanInfo.getClassName();
                Class clazz = Class.forName(className);
                if (log.isDebugEnabled()) {
                    log.debug("Object name : {}, {}, {}", new Object[]{objectName.getCanonicalName(), className, Arrays.toString(clazz.getInterfaces())});
                }
                if (RuntimeMXBean.class.isAssignableFrom(clazz)) {
                    misc(jMetrics, JMX.newMXBeanProxy(mbeanConn, objectName, RuntimeMXBean.class, true));
                } else if (ThreadMXBean.class.isAssignableFrom(clazz)) {
                    threading(jMetrics, JMX.newMXBeanProxy(mbeanConn, objectName, ThreadMXBean.class, true));
                } else if (MemoryPoolMXBean.class.isAssignableFrom(clazz)) {
                    memoryPool(jMetrics, Collections.singletonList(JMX.newMXBeanProxy(mbeanConn, objectName, MemoryPoolMXBean.class, true)));
                } else if (MemoryMXBean.class.isAssignableFrom(clazz)) {
                    memory(jMetrics, JMX.newMXBeanProxy(mbeanConn, objectName, MemoryMXBean.class, true));
                } else if (GarbageCollectorMXBean.class.isAssignableFrom(clazz)) {
                    garbageCollection(jMetrics, Collections.singletonList(JMX.newMXBeanProxy(mbeanConn, objectName, GarbageCollectorMXBean.class, true)));
                } else if (CompilationMXBean.class.isAssignableFrom(clazz)) {
                    compilation(jMetrics, JMX.newMXBeanProxy(mbeanConn, objectName, CompilationMXBean.class, true));
                } else if (ClassLoadingMXBean.class.isAssignableFrom(clazz)) {
                    classloading(jMetrics, JMX.newMXBeanProxy(mbeanConn, objectName, ClassLoadingMXBean.class, true));
                }
            } catch (final ClassNotFoundException | IntrospectionException | InstanceNotFoundException | IOException | ReflectionException e) {
                log.error("Exception accessing MBean : " + objectName, e);
            }
        }

        transport.postMetrics(jMetrics);
    }

    /**
     * Releases all resources that this object holds, typically the connection to the JMX beans, and cancels the task in the {@link java.util.Timer}
     *
     * @return whether all releases of resources was successful
     */
    @Override
    public boolean terminate() {
        if (jmxConnector != null) {
            try {
                jmxConnector.close();
            } catch (final IOException e) {
                log.warn("Exception disconnecting from JMX : ", e);
            }
        }
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }

    /**
     * TODO: Use the{@link com.pxs.reaper.action.ReaperAction.Retry} class dynamically
     */
    private MBeanServerConnection getMBeanServerConnection() {
        return getMBeanServerConnection(3, 1000);
    }

    private MBeanServerConnection getMBeanServerConnection(final int retry, final long delay) {
        if (mbeanConn == null) {
            JMXServiceURL serviceUrl;
            try {
                serviceUrl = new JMXServiceURL(reaperJmxUri);
                jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
                return mbeanConn = jmxConnector.getMBeanServerConnection();
            } catch (final Exception e) {
                if (retry > 0) {
                    long sleep = delay / retry;
                    THREAD.sleep(sleep);
                    return getMBeanServerConnection(retry - 1, delay);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        return mbeanConn;
    }

}