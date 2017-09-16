package com.pxs.reaper;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.hyperic.sigar.SigarException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import java.io.File;

@RunWith(JMockit.class)
public class ReaperTest {

    @SuppressWarnings("unused")
    public static class ContainerProviderMock extends MockUp<ContainerProvider> {
        @Mock
        public WebSocketContainer getWebSocketContainer() throws IOException, DeploymentException {
            WebSocketContainer webSocketContainer = Mockito.mock(WebSocketContainer.class);
            Session session = Mockito.mock(Session.class);
            RemoteEndpoint.Async async = Mockito.mock(RemoteEndpoint.Async.class);

            Mockito.when(webSocketContainer.connectToServer(Mockito.any(Reaper.class), Mockito.any(URI.class))).thenReturn(session);
            Mockito.when(session.getAsyncRemote()).thenReturn(async);
            return webSocketContainer;
        }
    }

    @Test
    public void reap() throws SigarException {
        String pwd = new File(".").getAbsolutePath();
        String message = "Build path : " + pwd;
        LOGGER.error(message);
        new ContainerProviderMock();
        new Reaper(10).reap();
    }

}
