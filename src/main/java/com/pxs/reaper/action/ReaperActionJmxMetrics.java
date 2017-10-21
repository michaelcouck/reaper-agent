package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import ikube.toolkit.THREAD;
import lombok.extern.slf4j.Slf4j;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.util.Set;

/**
 * The pid for the Java processes are often not exposed, for example on OpenShift, consequently it is not possible to dynamically attach a n agent
 * to the process. This class adapts the JMX beans from the local host, proxies them to conform to management MBeans, and invokes get operations to
 * extract telemetry and metrics information from the JVM.
 * <p>
 * <p>
 * Parameters to allow JMX access.
 * <pre>
 *      -Dcom.sun.management.jmxremote=false
 *      -Dcom.sun.management.jmxremote.local.only=true
 *      -Dcom.sun.management.jmxremote.authenticate=false
 *      -Dcom.sun.management.jmxremote.ssl=false
 *      -Djava.rmi.server.hostname=localhost
 *      -Dcom.sun.management.jmxremote.port=8500
 *      -Dcom.sun.management.jmxremote.rmi.port=8501
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-10-2017
 */
@Slf4j
@SuppressWarnings("WeakerAccess")
public class ReaperActionJmxMetrics extends ReaperActionMBeanMetrics {

    private Transport transport;
    private MBeanServerConnection mbeanConn;

    public ReaperActionJmxMetrics() {
        transport = new WebSocketTransport();
    }

    @Override
    public void run() {
        JMetrics jMetrics = JMetrics.builder().build();

        try {
            getMBeanServerConnection();

            Set<ObjectName> objectNames = mbeanConn.queryNames(null, null);
            for (final ObjectName objectName : objectNames) {
                log.info("Object name : " + objectName.getCanonicalName());
                if (objectName.getCanonicalName().contains("Runtime")) {
                    RuntimeMXBean runtimeMXBean = JMX.newMXBeanProxy(mbeanConn, objectName, RuntimeMXBean.class, true);
                    misc(jMetrics, runtimeMXBean);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }

        //noinspection ConstantConditions,ConstantIfStatement
        if (false) {
            misc(jMetrics, null);
            classloading(jMetrics, null);
            compilation(jMetrics, null);
            garbageCollection(jMetrics, null);
            memory(jMetrics, null);
            memoryPool(jMetrics, null);
            threading(jMetrics, null);
        }

        transport.postMetrics(jMetrics);
    }


    @Override
    public boolean terminate() {
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }

    private MBeanServerConnection getMBeanServerConnection() {
        return getMBeanServerConnection(5, 1000);
    }

    private MBeanServerConnection getMBeanServerConnection(final int retry, final long delay) {
        if (mbeanConn == null) {
            JMXServiceURL serviceUrl;
            String url = "service:jmx:rmi:///jndi/rmi://localhost:8500/jmxrmi";
            try {
                serviceUrl = new JMXServiceURL(url);
                JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
                return mbeanConn = jmxConnector.getMBeanServerConnection();
            } catch (final Exception e) {
                if (retry > 0) {
                    THREAD.sleep(delay / retry * 1000);
                    return getMBeanServerConnection(retry - 1, delay);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        return mbeanConn;
    }

}