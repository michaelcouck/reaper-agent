package com.pxs.reaper.action;

import org.apache.log4j.Logger;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class ReaperActionJMXMetrics implements ReaperAction, NotificationListener, Runnable {

    private final Logger logger = Logger.getLogger(this.getClass());

    private final URI uri = URI.create("ws://reaper-microservice-reaper.b9ad.pro-us-east-1.openshiftapps.com/reaper-websocket");

    @Override
    public void run() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        Session session = null;
        try {
            session = container.connectToServer(this, uri);
            RemoteEndpoint.Async async = session.getAsyncRemote();
            async.sendText(GSON.toJson("bla..."));
        } catch (final DeploymentException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (final IOException e) {
                    logger.error("Exception closing session in JMX reaper : ", e);
                }
            }
        }
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