package com.pxs.reaper.transport;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
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
        Constant.PROPERTIES_INJECTOR.injectProperties(restTransport);
        JMetrics jMetrics = new JMetrics();
        restTransport.postMetrics(jMetrics);
    }

}
