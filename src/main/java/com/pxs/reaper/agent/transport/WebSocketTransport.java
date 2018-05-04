package com.pxs.reaper.agent.transport;

import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.model.Message;
import com.pxs.reaper.agent.model.Metrics;
import com.pxs.reaper.agent.toolkit.Retry;
import com.pxs.reaper.agent.toolkit.RetryIncreasingDelay;
import com.pxs.reaper.agent.toolkit.THREAD;
import lombok.Setter;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.PropertiesInjector;

import javax.websocket.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transport for the web socket implementation. This implementation connects to the web socket
 * using a parameter in the properties file, converts the metrics objects for TRANSPORT into Json.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@SuppressWarnings("deprecation")
@Setter
@Deprecated
@ClientEndpoint
public class WebSocketTransport implements Transport {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    /**
     * The uri to the central analyzer for posting metrics to
     */
    @Property(key = Constant.WEB_SOCKET_URI, source = Constant.REAPER_PROPERTIES)
    private String reaperWebSocketUri;
    /**
     * Delay between logging the metrics posted
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "logging-interval")
    private long loggingInterval;
    /**
     * Time stamp for the last time a LOG was posted
     */
    private long lastLoggingTimestamp;
    /**
     * Maximum number of retries to connect to the service
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "max-retries")
    private int maxRetries;
    /**
     * The maximum delay between retries to connect ot the service
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "final-retry-delay")
    private long finalRetryDelay;

    /**
     * Retry class for trying to connect to delivery protocol.
     */
    private Retry retryWithIncreasingDelay;
    /**
     * Reference to the session to the centralized analyzer
     */
    private Session session;

    public WebSocketTransport() {
        loggingInterval = 1000 * 60 * 10;
        lastLoggingTimestamp = System.currentTimeMillis();
        retryWithIncreasingDelay = new RetryIncreasingDelay();

        PropertiesInjector propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
        propertiesInjector.injectProperties(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean postMetrics(final Object metrics) {
        openSession();
        // String postage = Constant.GSON.toJson(metrics);
        try {
            // Periodically LOG some data
            if (System.currentTimeMillis() - lastLoggingTimestamp > loggingInterval) {
                lastLoggingTimestamp = System.currentTimeMillis();
                String ipAddress = ((Metrics) metrics).getIpAddress();
                String codeBase = ((Metrics) metrics).getCodeBase();
                log.info("Sent metrics : " + reaperWebSocketUri + ", address : " + ipAddress + " : " + codeBase + " : " + metrics.getClass().getSimpleName());
            }

            // LOG.info(postage);
            RemoteEndpoint.Async async = session.getAsyncRemote();
            // Future<Void> future = async.sendText(postage);
            Future<Void> future = async.sendObject(metrics);
            future.get(1000, TimeUnit.MILLISECONDS);

            // Another possibility... Rather than async, sync.
            // RemoteEndpoint.Basic basic = session.getBasicRemote();
            // basic.sendText(postage);
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Exception posting metrics, is the service up? : " + session, e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * Opens a session to the web socket of the centralized analyzer.
     */
    private void openSession() {
        Function<Void, Session> function = aVoid -> {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = URI.create(reaperWebSocketUri);
            try {
                log.log(Level.INFO, "Session null : " + session + ", session open : " + (session != null ? session.isOpen() : null));
                if (session == null || !session.isOpen()) {
                    log.log(Level.INFO, "Opening(re) web socket session with uri : {}", reaperWebSocketUri);
                    session = container.connectToServer(WebSocketTransport.this, uri);
                    session.setMaxIdleTimeout(600000);
                }
                return session;
            } catch (final DeploymentException | IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (session == null || !session.isOpen()) {
                    log.log(Level.INFO, "Websocket session not open : ");
                } else {
                    try {
                        session.getBasicRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
                    } catch (final IOException e) {
                        log.log(Level.SEVERE, "Couldn't ping target micro service : ", e);
                    }
                }
            }
        };
        if (session == null || !session.isOpen()) {
            if (session != null) {
                try {
                    session.close();
                } catch (final Exception e) {
                    log.log(Level.SEVERE, "Couldn't close web socket connection : ", e);
                }
            }
            retryWithIncreasingDelay.retry(function, null, maxRetries, finalRetryDelay);
        }
    }

    /**
     * {@inheritDoc}
     */
    @OnOpen
    public void onOpen(final Session session) throws IOException {
        log.log(Level.INFO, "Session opened : " + session);
    }

    /**
     * {@inheritDoc}
     */
    @OnMessage
    @SuppressWarnings({"UnusedParameters", "ConstantConditions"})
    public void onMessage(final String message, final Session session) throws IOException {
        // ObjectMapper objectMapper = new ObjectMapper();
        Message m = null; // Constant.GSON.fromJson(message, Message.class);
        if (m != null && m.isTerminate()) {
            log.log(Level.INFO, "Cancelling agent in JVM : " + ManagementFactory.getRuntimeMXBean().getName());
            // TODO: Change this to scheduled executor rather, and kill the individual tasks rather because
            // TODO: we might want to start the tasks again. This also entails a manager of some sort for creating
            // TODO: the tasks so we can create them again if necessary, with different parameters
            THREAD.cancelScheduledFutures();
        } else if (m.isStart()) {
            THREAD.startScheduledFutures();
        }
    }

    /**
     * {@inheritDoc}
     */
    @OnClose
    public void onClose(final Session session) {
        log.log(Level.FINE, "Session closed : " + session);
    }

    /**
     * {@inheritDoc}
     */
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        log.log(Level.FINE, "Error in session : " + session, throwable);
    }

}