package com.pxs.reaper.agent.transport;

import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.model.OSMetrics;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.PropertiesInjector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@Setter
@Getter
@Ignore
@Deprecated
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
        PropertiesInjector propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
        propertiesInjector.injectProperties(this);
    }

    @Test
    public void postMetrics() {
        webSocketTransport.setReaperWebSocketUri(reaperWebSocketUri);
        OSMetrics osMetrics = new OSMetrics();
        boolean posted = webSocketTransport.postMetrics(osMetrics);
        Assert.assertTrue(posted);
    }

}
