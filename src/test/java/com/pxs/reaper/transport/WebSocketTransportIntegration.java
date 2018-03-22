package com.pxs.reaper.transport;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.toolkit.FILE;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.annotations.Property;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

@Setter
@Getter
@RunWith(MockitoJUnitRunner.class)
public class WebSocketTransportIntegration {

    /**
     * This class must not be a spy from mockito because cglib removes the
     * annotation, and the configuration for the client session to the server fails
     * because of that.
     */
    private WebSocketTransport webSocketTransport;

    @Property(key = Constant.WEB_SOCKET_URI, source = Constant.REAPER_PROPERTIES)
    private String reaperWebSocketUri;

    @Before
    public void before() {
        webSocketTransport = new WebSocketTransport();
        Constant.PROPERTIES_INJECTOR.injectProperties(this);
    }

    @Test
    public void postMetrics() {
        webSocketTransport.setReaperWebSocketUri(reaperWebSocketUri);

        File file = FILE.findFileRecursively(new File("."), "o-metrics.json");
        String json = FILE.getContent(file);
        OSMetrics osMetrics = Constant.GSON.fromJson(json, OSMetrics.class);
        boolean posted = webSocketTransport.postMetrics(osMetrics);
        Assert.assertTrue(posted);
    }

}
