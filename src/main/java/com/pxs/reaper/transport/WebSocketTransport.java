package com.pxs.reaper.transport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pxs.reaper.Constant;
import com.pxs.reaper.toolkit.Retry;
import com.pxs.reaper.toolkit.RetryIncreasingDelay;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.props.annotations.Property;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.function.Function;

/**
 * Transport for the web socket implementation. This implementation connects to the web socket using a parameter in the
 * properties file, converts the metrics objects for transport into Json.
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
    @SuppressWarnings("unused")
    @Property(source = Constant.REAPER_PROPERTIES, key = "reaper-web-socket-uri")
    private String reaperWebSocketUri;
    /**
     * Retry class for trying to connect to delivery protocol.
     */
    private Retry retryWithIncreasingDelay;
    /**
     * Converts the metrics objects to json for transport over the wire
     */
    private Gson gson;
    /**
     * Reference to the session to the centralized analyzer
     */
    private Session session;
    /**
     * Delay between logging the metrics posted.
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "logging-interval")
    private long loggingInterval = 1000 * 60 * 60;
    private long lastLoggingTimestamp = System.currentTimeMillis();
    @Property(source = Constant.REAPER_PROPERTIES, key = "max-retries")
    private int maxRetries;
    @Property(source = Constant.REAPER_PROPERTIES, key = "final-retry-delay")
    private long finalRetryDelay;

    public WebSocketTransport() {
        gson = new GsonBuilder().create();
        Constant.PROPERTIES_INJECTOR.injectProperties(this);
        retryWithIncreasingDelay = new RetryIncreasingDelay();
    }

    /**
     * {@inheritDoc}
     */
    public void postMetrics(final Object metrics) {
        openSession();

        RemoteEndpoint.Async async = session.getAsyncRemote();
        String postage = gson.toJson(metrics);
        // Periodically log some data
        if (System.currentTimeMillis() - lastLoggingTimestamp > loggingInterval) {
            log.info("Sending metrics : {}", postage);
            lastLoggingTimestamp = System.currentTimeMillis();
        }
        log.debug("Sending metrics : {}", postage);
        async.sendText(postage);
    }

    /**
     * Opens a session to the web socket of the centralized analyzer.
     */
    private void openSession() {
        if (session == null || !session.isOpen()) {
            Transport transport = this;
            Function<Void, Session> function = aVoid -> {
                WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                URI uri = URI.create(reaperWebSocketUri);
                try {
                    return container.connectToServer(transport, uri);
                } catch (DeploymentException | IOException e) {
                    throw new RuntimeException(e);
                }
            };
            session = retryWithIncreasingDelay.retry(function, null, maxRetries, finalRetryDelay);
        }
    }

    /**
     * {@inheritDoc}
     */
    @OnOpen
    public void onOpen(final Session session) throws IOException {
        log.debug("Session opened : " + session.getId());
    }

    /**
     * {@inheritDoc}
     */
    @OnMessage
    @SuppressWarnings("UnusedParameters")
    public void onMessage(final String message, final Session session) throws IOException {
        log.debug("Got message : " + message);
    }

    /**
     * {@inheritDoc}
     */
    @OnClose
    public void onClose(final Session session) {
        log.debug("Session closed : " + session.getId());
    }

    /**
     * {@inheritDoc}
     */
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        log.error("Error in session : " + session.getId(), throwable);
    }

}