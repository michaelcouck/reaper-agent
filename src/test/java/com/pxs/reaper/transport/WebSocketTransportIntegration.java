package com.pxs.reaper.transport;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.toolkit.FILE;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class WebSocketTransportIntegration {

    private WebSocketTransport webSocketTransport;

    @Before
    public void before() {
        webSocketTransport = new WebSocketTransport();
    }

    @Test
    public void postMetrics() {
        File file = FILE.findFileRecursively(new File("."), "o-metrics.json");
        String json = FILE.getContent(file);
        OSMetrics osMetrics = Constant.GSON.fromJson(json, OSMetrics.class);
        boolean posted = webSocketTransport.postMetrics(osMetrics);
        Assert.assertTrue(posted);
    }

}
