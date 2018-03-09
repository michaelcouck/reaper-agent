package com.pxs.reaper.transport;

import com.pxs.reaper.model.JMetrics;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestTransportIntegration {

    @Spy
    private RestTransport restTransport;

    @Test
    public void postMetrics() {
        JMetrics jMetrics = new JMetrics();
        boolean success = restTransport.postMetrics(jMetrics);
        Assert.assertTrue(success);
    }

}
