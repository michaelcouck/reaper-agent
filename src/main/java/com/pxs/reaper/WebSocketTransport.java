package com.pxs.reaper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.props.annotations.Property;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@Slf4j
@Setter
@ClientEndpoint
public class WebSocketTransport implements Transport {

    @SuppressWarnings("unused")
    @Property(source = Constant.REAPER_PROPERTIES, key = "reaper-web-socket-uri")
    private String reaperWebSocketUri;

    private Session session;

    public WebSocketTransport() {
        Constant.PROPERTIES_INJECTOR.injectProperties(this);
    }

    public void postMetrics(final Object metrics) {
        getTransport();
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson GSON = gsonBuilder.create();
        RemoteEndpoint.Async async = session.getAsyncRemote();
        String postage = GSON.toJson(metrics);

        log.info("Metrics : {}", postage);

        async.sendText(postage);
    }

    private void getTransport() {
        if (session == null || !session.isOpen()) {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = URI.create(reaperWebSocketUri);
            try {
                session = container.connectToServer(this, uri);
            } catch (final DeploymentException | IOException e) {
                // TODO: Keep re-trying, service could be down
                throw new RuntimeException("Error connecting to : " + uri, e);
            }
        }
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

}
