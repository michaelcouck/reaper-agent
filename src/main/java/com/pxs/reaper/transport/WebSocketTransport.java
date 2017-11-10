package com.pxs.reaper.transport;

import com.pxs.reaper.Constant;
import com.pxs.reaper.toolkit.Retry;
import com.pxs.reaper.toolkit.RetryIncreasingDelay;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.props.annotations.Property;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Transport for the web socket implementation. This implementation connects to the web socket
 * using a parameter in the properties file, converts the metrics objects for TRANSPORT into Json.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Slf4j
@Setter
@ClientEndpoint
public class WebSocketTransport implements Transport {

    /**
     * The uri to the central analyzer for posting metrics to
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "reaper-web-socket-uri")
    private String reaperWebSocketUri;
    /**
     * Delay between logging the metrics posted
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "logging-interval")
    private long loggingInterval;
    /**
     * Time stamp for the last time a log was posted
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

        Constant.PROPERTIES_INJECTOR.injectProperties(this);
    }

    /**
     * {@inheritDoc}
     */
    public void postMetrics(final Object metrics) {
        openSession();
        String postage = Constant.GSON.toJson(metrics);
        // Periodically log some data
        if (System.currentTimeMillis() - lastLoggingTimestamp > loggingInterval) {
            log.info("Sending metrics : {}", postage);
            lastLoggingTimestamp = System.currentTimeMillis();
        }
        RemoteEndpoint.Async async = session.getAsyncRemote();
        Future<Void> future = async.sendText(postage);
        try {
            future.get(1000, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Timed out waiting to post metrics, is the service up? : ", e);
        }
    }

    /**
     * Opens a session to the web socket of the centralized analyzer.
     */
    private void openSession() {
        if (session != null && session.isOpen()) {
            return;
        }
        WebSocketTransport webSocketTransport = this;
        Function<Void, Session> function = aVoid -> {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = URI.create(reaperWebSocketUri);
            try {
                log.debug("Re-opening web socket session : ");
                return container.connectToServer(webSocketTransport, uri);
            } catch (final DeploymentException | IOException e) {
                throw new RuntimeException(e);
            }
        };
        session = retryWithIncreasingDelay.retry(function, null, maxRetries, finalRetryDelay);
    }

    /**
     * {@inheritDoc}
     */
    @OnOpen
    public void onOpen(final Session session) throws IOException {
        log.debug("Session opened : {}", session.getId());
    }

    /**
     * {@inheritDoc}
     */
    @OnMessage
    @SuppressWarnings("UnusedParameters")
    public void onMessage(final String message, final Session session) throws IOException {
        log.debug("Got message : {}", message);
    }

    /**
     * {@inheritDoc}
     */
    @OnClose
    public void onClose(final Session session) {
        log.debug("Session closed : {}", session.getId());
    }

    /**
     * {@inheritDoc}
     */
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        log.error("Error in session : {}", session.getId(), throwable);
    }

}