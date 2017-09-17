package com.pxs.reaper.action;

import com.sun.management.DiagnosticCommandMBean;
import com.sun.management.HotSpotDiagnosticMXBean;
import lombok.extern.slf4j.Slf4j;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.websocket.*;
import java.io.IOException;
import java.lang.management.*;
import java.net.URI;
import java.util.Set;
import java.util.logging.LoggingMXBean;

@Slf4j
@ClientEndpoint
public class ReaperActionJMXMetrics implements ReaperAction, NotificationListener, Runnable {

    private final Session session;

    /**
     * -Dcom.sun.management.jmxremote=false
     * -Dcom.sun.management.jmxremote.local.only=true
     * -Dcom.sun.management.jmxremote.authenticate=false
     * -Dcom.sun.management.jmxremote.ssl=false
     * -Djava.rmi.server.hostname=localhost
     * -Dcom.sun.management.jmxremote.port=9999
     * -Dcom.sun.management.jmxremote.rmi.port=9998
     *
     * @param reaperWebSocketUri url to the collector
     */
    @SuppressWarnings("unused")
    public ReaperActionJMXMetrics(final String reaperWebSocketUri) {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = URI.create(reaperWebSocketUri);
        try {
            session = container.connectToServer(this, uri);
        } catch (final DeploymentException | IOException e) {
            throw new RuntimeException("Error connecting to : " + uri, e);
        }

        int port = 8500;
        String host = "localhost";  // or some A.B.C.D
        String url = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
        JMXServiceURL serviceUrl;
        try {
            serviceUrl = new JMXServiceURL(url);
            JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, null);
            MBeanServerConnection mbeanConn = jmxConnector.getMBeanServerConnection();
            Set<ObjectName> beanSet = mbeanConn.queryNames(null, null);
            for (final ObjectName objectName : beanSet) {
                log.warn("Object name : " + objectName);
                // mbeanConn.addNotificationListener(objectName, this, null, null);
            }
        } catch (final Exception e) {
            log.warn("Couldn't connect to localhost JMS", e);
        }
        MemoryPoolMXBean metaspace;
        MemoryPoolMXBean psOldGen;
        MemoryPoolMXBean psEdenSpace;
        MemoryPoolMXBean codeCache;
        MemoryPoolMXBean compressedClassSpace;
        MemoryPoolMXBean psSurvivorSpace;

        MemoryManagerMXBean codeCacheManager;
        MemoryManagerMXBean memoryManagerMXBean;
        MemoryManagerMXBean metaspaceManager;

        BufferPoolMXBean mapped;
        BufferPoolMXBean bufferPoolMXBean;

        ThreadMXBean threadMXBean;
        RuntimeMXBean runtimeMXBean;
        GarbageCollectorMXBean psScavenge;
        OperatingSystemMXBean operatingSystemMXBean;
        LoggingMXBean loggingMXBean;
        ClassLoadingMXBean classLoadingMXBean;
        DiagnosticCommandMBean diagnosticCommandMBean;
        GarbageCollectorMXBean psMarkSweep;
        HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean;
    }

    @Override
    @SuppressWarnings("unused")
    public void run() {
        RemoteEndpoint.Async async = session.getAsyncRemote();
        // async.sendText(GSON.toJson("bla..."));
    }

    @OnOpen
    public void onOpen(final Session session) throws IOException {
        log.debug("Session opened : " + session.getId());
    }

    @OnMessage
    @SuppressWarnings("UnusedParameters")
    public void onMessage(final String message, final Session session) throws IOException {
        log.info("Got message : " + message);
    }

    @OnClose
    public void onClose(final Session session) {
        log.debug("Session closed : " + session.getId());
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        log.error("Error in session : " + session.getId(), throwable);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
    }
}