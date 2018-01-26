package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.toolkit.HOST;
import com.pxs.reaper.toolkit.Retry;
import com.pxs.reaper.toolkit.RetryIncreasingDelay;
import lombok.Setter;
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
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 *      -Dcom.sun.management.jmxremote.rmi.port=1100
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
@Setter
@SuppressWarnings("WeakerAccess")
public class ReaperActionJmxMetrics extends ReaperActionMetrics {

    private final Logger log = Logger.getLogger(this.getClass().getSimpleName());

    /**
     * The uri to post the metrics to
     */
    // @SystemProperty("localhost-jmx-uri")
    @Property(source = Constant.REAPER_PROPERTIES, key = "localhost-jmx-uri")
    private String reaperJmxUri = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
    /**
     * Time to sleep between accessing the JMX telemetry.
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
    private long sleepTime = 1000 * 60 * 15;
    @Property(source = Constant.REAPER_PROPERTIES, key = "max-retries")
    private long maxRetries;
    @Property(source = Constant.REAPER_PROPERTIES, key = "final-retry-delay")
    private long finalRetryDelay;

    /**
     * The JMX connector to the Java process, instance variable so as to release resources on terminate
     */
    private JMXConnector jmxConnector;
    /**
     * The management bean connection to the JMX MBeans, instance variable so as to release resources on terminate
     */
    private MBeanServerConnection mbeanConn;
    /**
     * A class to retryWithIncreasingDelay certain functions with a sleepTime.
     */
    private Retry retryWithIncreasingDelay;

    public ReaperActionJmxMetrics() {
        retryWithIncreasingDelay = new RetryIncreasingDelay();
        log.fine("Attempting to attach to JMX system : " + HOST.hostname());
    }

    /**
     * Connects to the local JMX management beans. Iterates through all the beans, extracting interesting metrics and telemetry data
     * from the beans. Populates a {@link JMetrics} object that is converted to json for TRANSPORT the central analyzer.
     */
    @Override
    public void run() {
        // TODO: Scan for all ports here, just in case... Then save them.
        // TODO: Perhaps have a scheduled scan to check for mew exposures.
        Set<ObjectName> objectNames = getObjectNames();
        if (objectNames == null) {
            log.fine("No connection to JMX : ");
            return;
        }

        JMetrics jMetrics = new JMetrics();
        for (final ObjectName objectName : objectNames) {
            try {
                MBeanInfo mBeanInfo = mbeanConn.getMBeanInfo(objectName);
                String className = mBeanInfo.getClassName();
                Class clazz = Class.forName(className);
                log.fine("Object name : " +
                        Arrays.toString(new Object[]{objectName.getCanonicalName() +
                                ":" + className +
                                ":" + Arrays.toString(clazz.getInterfaces())}));
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
                } else if (OperatingSystemMXBean.class.isAssignableFrom(clazz)) {
                    os(jMetrics, JMX.newMXBeanProxy(mbeanConn, objectName, OperatingSystemMXBean.class, true));
                }
            } catch (final ClassNotFoundException | IntrospectionException | InstanceNotFoundException | IOException | ReflectionException e) {
                log.log(Level.SEVERE, "Exception accessing MBean : " + objectName, e);
            }
        }

        Constant.TRANSPORT.postMetrics(jMetrics);
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
                log.log(Level.SEVERE, "Exception disconnecting from JMX : ", e);
            }
        }
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }

    private Set<ObjectName> getObjectNames() {
        getMBeanServerConnection();
        Function<Void, Set<ObjectName>> function = aVoid -> {
            try {
                if (mbeanConn == null) {
                    return null;
                }
                return mbeanConn.queryNames(null, null);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        };
        return retryWithIncreasingDelay.retry(function, null, (int) maxRetries, finalRetryDelay);
    }

    private MBeanServerConnection getMBeanServerConnection() {
        Function<Void, MBeanServerConnection> function = aVoid -> {
            if (mbeanConn != null) {
                return mbeanConn;
            }
            try {
                log.fine("JMX url : " + reaperJmxUri);
                JMXServiceURL serviceUrl = new JMXServiceURL(reaperJmxUri);
                jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
                return mbeanConn = jmxConnector.getMBeanServerConnection();
            } catch (final Exception e) {
                log.log(Level.FINEST, "No jmx on this machine : ");
            }
            return null;
        };
        try {
            return retryWithIncreasingDelay.retry(function, null, 5, sleepTime);
        } catch (final Exception e) {
            log.log(Level.FINEST, "No jmx on this machine : ");
            return null;
        }
    }

}