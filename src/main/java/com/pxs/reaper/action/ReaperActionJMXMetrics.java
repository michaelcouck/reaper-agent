package com.pxs.reaper.action;

import org.apache.log4j.Logger;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class ReaperActionJMXMetrics implements ReaperAction, NotificationListener, Runnable {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final Session session;
    private final URI uri = URI.create("ws://reaper-microservice-reaper.b9ad.pro-us-east-1.openshiftapps.com/reaper-websocket");

    public ReaperActionJMXMetrics() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            session = container.connectToServer(this, uri);
        } catch (final DeploymentException | IOException e) {
            throw new RuntimeException("Error connecting to : " + uri, e);
        }
    }

    @Override
    public void run() {
        RemoteEndpoint.Async async = session.getAsyncRemote();
        // async.sendText(GSON.toJson("bla..."));
    }

    @OnOpen
    public void onOpen(final Session session) throws IOException {
        logger.debug("Session opened : " + session.getId());
    }

    @OnMessage
    @SuppressWarnings("UnusedParameters")
    public void onMessage(final String message, final Session session) throws IOException {
        logger.info("Got message : " + message);
    }

    @OnClose
    public void onClose(final Session session) {
        logger.debug("Session closed : " + session.getId());
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        logger.error("Error in session : " + session.getId(), throwable);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
    }
}