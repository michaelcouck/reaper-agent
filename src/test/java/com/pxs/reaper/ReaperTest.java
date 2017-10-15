package com.pxs.reaper;

import lombok.extern.slf4j.Slf4j;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.websocket.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(JMockit.class)
public class ReaperTest {

    /**
     * Mocks the web socket container.
     */
    public static class ContainerProviderMock extends MockUp<ContainerProvider> {
        @Mock
        @SuppressWarnings("unused")
        public WebSocketContainer getWebSocketContainer() throws IOException, DeploymentException {
            WebSocketContainer webSocketContainer = mock(WebSocketContainer.class);
            Session session = mock(Session.class);
            RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);

            when(webSocketContainer.connectToServer(any(Reaper.class), any(URI.class))).thenReturn(session);
            when(session.getAsyncRemote()).thenReturn(async);
            return webSocketContainer;
        }
    }

    @Test
    public void attachToOperatingSystem() throws Exception {
        new ContainerProviderMock();
        Reaper reaper = new Reaper();
        reaper.attachToOperatingSystem();
    }

    @Test
    public void attachToJavaProcesses() throws Exception {
        new ContainerProviderMock();
        Reaper reaper = new Reaper();
        reaper.attachToJavaProcesses();
        reaper.attachToJavaProcesses();
    }

    @Test
    public void addNativeLibrariesToPath() throws IOException {
        String linuxLoadModule = "libsigar-amd64-linux.so";
        String javaLibraryPath = Reaper.addNativeLibrariesToPath();
        String[] paths = StringUtils.split(javaLibraryPath, File.pathSeparatorChar);
        for (final String path : paths) {
            if (Arrays.deepToString(new File(path).list()).contains(linuxLoadModule)) {
                return;
            }
        }
        Assert.fail("Should contain the link libraries");
    }

}
