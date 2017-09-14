package com.pxs.reaper.action;

import org.apache.log4j.Logger;

import javax.websocket.*;
import java.io.IOException;

abstract class ReaperAAction implements ReaperAction {

    final Logger logger = Logger.getLogger(this.getClass());

    @OnOpen
    public void onOpen(final Session session) throws IOException {
        logger.debug("Session opened : " + session.getId());
    }

    @OnMessage
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
        onClose(session);
    }

}
