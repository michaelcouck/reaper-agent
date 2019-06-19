package com.pxs.reaper.agent.transport;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.model.OSMetrics;
import com.pxs.reaper.agent.toolkit.FILE;
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

import java.io.File;
import java.util.logging.Logger;

@Getter
@Setter
@RunWith(MockitoJUnitRunner.class)
public class ElasticTransportIntegration {

    private static Logger log = Logger.getLogger(ElasticTransportIntegration.class.getSimpleName());

    @Spy
    private ElasticTransport restTransport;

    @SuppressWarnings("unused")
    @Property(key = Constant.ELASTIC_URI, source = Constant.REAPER_PROPERTIES)
    private String elasticUri;

    @Setter
    @Getter
    public static class AnnotationProperty {

        @Property(key = Constant.ELASTIC_URI, source = Constant.REAPER_PROPERTIES)
        private String elasticUri;

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
        Assert.assertTrue(annotationProperty.getElasticUri().contains("/reaper"));
    }

    @Test
    public void postMetrics() {
        restTransport.setElasticUri(elasticUri);

        File file = FILE.findFileRecursively(new File("."), "o-metrics.json");
        String json = FILE.getContent(file);
        OSMetrics osMetrics = new Gson().fromJson(json, OSMetrics.class);

        boolean success = restTransport.postMetrics(osMetrics);
        Assert.assertTrue(success);
    }

    @Test
    public void test() throws UnirestException {
        String json = "{\"name\" : \"Michael Couck\"}";
        HttpResponse httpResponse = Unirest.post("http://192.168.1.70:9200/reaper/_doc/3").header("Content-Type", "application/json").body(json).asString();
    }

}