package com.pxs.reaper.transport;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.annotations.Property;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestTransportIntegration {

    @Spy
    private RestTransport restTransport;

    @Setter
    @Getter
    public static class AnnotationProperty {

        @Property(key = Constant.REST_URI_J_METRICS, source = Constant.REAPER_PROPERTIES)
        private String reaperJMetricsRestUri;

        public AnnotationProperty() {
            Constant.PROPERTIES_INJECTOR.injectProperties(this);
        }

    }

    @Test
    public void annotationProperty() {
        AnnotationProperty annotationProperty = new AnnotationProperty();
        Assert.assertEquals("http://ikube.be:8090/j-metrics", annotationProperty.getReaperJMetricsRestUri());
    }

    @Test
    public void postMetrics() {
        JMetrics jMetrics = new JMetrics();
        boolean success = restTransport.postMetrics(jMetrics);
        Assert.assertTrue(success);
    }

}
