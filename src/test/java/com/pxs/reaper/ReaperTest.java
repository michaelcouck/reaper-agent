package com.pxs.reaper;

import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.hyperic.sigar.SigarException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;

@RunWith(JMockit.class)
// TO mock static methods
// @Mocked({"mockMethod"})
public class ReaperTest {

    public class ContainerProviderMock extends MockUp<ContainerProvider> {
        @Mock
        public Session connectToServer(final Object object, final URI uri) throws DeploymentException, IOException {
            return Mockito.mock(Session.class);
        }
    }

    public class ContainerProvider extends MockUp<ContainerProvider> {
        @Mock
        public WebSocketContainer getWebSocketContainer() {
            return Mockito.mock(WebSocketContainer.class);
        }
    }

    @Test
    public void reap() throws SigarException {
        new Reaper(10).reap();
    }

}
