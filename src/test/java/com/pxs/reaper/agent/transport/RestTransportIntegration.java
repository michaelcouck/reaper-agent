package com.pxs.reaper.agent.transport;

import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.model.JMetrics;
import com.pxs.reaper.agent.model.OSMetrics;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.PropertiesInjector;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@Getter
@Setter
@RunWith(MockitoJUnitRunner.class)
public class RestTransportIntegration {

    @Spy
    private RestTransport restTransport;

    @SuppressWarnings("unused")
    @Property(key = Constant.REST_URI_J_METRICS, source = Constant.REAPER_PROPERTIES)
    private String reaperJMetricsRestUri;
    @SuppressWarnings("unused")
    @Property(key = Constant.REST_URI_O_METRICS, source = Constant.REAPER_PROPERTIES)
    private String reaperOMetricsRestUri;

    @Setter
    @Getter
    public static class AnnotationProperty {

        @Property(key = Constant.REST_URI_J_METRICS, source = Constant.REAPER_PROPERTIES)
        private String reaperJMetricsRestUri;

        public AnnotationProperty() {
            PropertiesInjector propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
            propertiesInjector.injectProperties(this);
        }

    }

    @Before
    public void before() {
        PropertiesInjector propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
        propertiesInjector.injectProperties(this);
    }

    @Test
    public void annotationProperty() {
        AnnotationProperty annotationProperty = new AnnotationProperty();
        Assert.assertTrue(annotationProperty.getReaperJMetricsRestUri().contains("/j-metrics"));
    }

    @Test
    public void postMetrics() {
        restTransport.setReaperJMetricsRestUri(reaperJMetricsRestUri);
        restTransport.setReaperOMetricsRestUri(reaperOMetricsRestUri);
        JMetrics jMetrics = new JMetrics();
        boolean success = restTransport.postMetrics(jMetrics);
        Assert.assertTrue(success);
        OSMetrics osMetrics = new OSMetrics();
        success = restTransport.postMetrics(osMetrics);
        Assert.assertTrue(success);
    }

}
