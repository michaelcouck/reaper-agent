package com.pxs.reaper.transport;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.model.OSMetrics;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.annotations.SystemProperty;

import java.util.logging.Logger;

@Setter
@Getter
public class RestTransport implements Transport {

    private static Logger log = Logger.getLogger(RestTransport.class.getSimpleName());

    @SystemProperty(value = "reaper-rest-uri-j-metrics", defaultValue = "http://ikube.be:8090/j-metrics")
    private String reaperJMetricsRestUri;
    @SystemProperty(value = "reaper-rest-uri-o-metrics", defaultValue = "http://ikube.be:8090/o-metrics")
    private String reaperOMetricsRestUri;

    public RestTransport() {
        log.info("System properties : " + System.getProperties());
        Constant.PROPERTIES_INJECTOR.injectProperties(this);
    }

    @Override
    public boolean postMetrics(final Object metrics) {
        try {
            String json = Constant.GSON.toJson(metrics);
            if (JMetrics.class.isAssignableFrom(metrics.getClass())) {
                Unirest.post(reaperJMetricsRestUri).body(json).asString();
            } else if (OSMetrics.class.isAssignableFrom(metrics.getClass())) {
                Unirest.post(reaperOMetricsRestUri).body(json).asString();
            } else {
                log.warning("No endpoint for object : " + metrics);
            }
        } catch (final UnirestException e) {
            throw new RuntimeException("Exception posting to the micro service : ", e);
        }
        return true;
    }

}