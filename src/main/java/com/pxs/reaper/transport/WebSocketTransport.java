package com.pxs.reaper.transport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pxs.reaper.Constant;
import com.pxs.reaper.toolkit.THREAD;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.props.annotations.Property;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

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
     * Converts the metrics objects to json for transport over the wire
     */
    private Gson gson;
    /**
     * Reference to the session to the centralized analyzer
     */
    private Session session;

    private long loggingInterval = 1000 * 60 * 60;
    private long lastLoggingTimestamp = System.currentTimeMillis();

    public WebSocketTransport() {
        gson = new GsonBuilder().create();
        Constant.PROPERTIES_INJECTOR.injectProperties(this);
    }

    /**
     * {@inheritDoc}
     */
    public void postMetrics(final Object metrics) {
        openSession(3, 1000);

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
     *
     * @param retry the number of times to retry opening the connection
     * @param delay the final delay, if set to 1000 eg. then the first delay will be 333ms, then 500ms, then 1000ms
     */
    private void openSession(final int retry, final long delay) {
        if (session == null || !session.isOpen()) {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = URI.create(reaperWebSocketUri);
            try {
                session = container.connectToServer(this, uri);
            } catch (final DeploymentException | IOException e) {
                if (retry > 0) {
                    THREAD.sleep(delay / retry);
                    openSession(retry - 1, delay);
                    return;
                }
                throw new RuntimeException("Error connecting to : " + uri, e);
            }
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