package com.pxs.reaper;

import lombok.extern.slf4j.Slf4j;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.SigarException;
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

    @SuppressWarnings("unused")
    public static class ContainerProviderMock extends MockUp<ContainerProvider> {
        @Mock
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
    public void reap() throws SigarException, IOException {
        new ContainerProviderMock();
        Reaper reaper = new Reaper();
        reaper.reap();
        // TODO: What is the result of this call, i.e. functional output
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
