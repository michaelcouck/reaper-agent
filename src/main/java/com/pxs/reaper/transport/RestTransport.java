package com.pxs.reaper.transport;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.model.OSMetrics;
import lombok.Setter;
import org.jeasy.props.annotations.Property;

import java.util.logging.Logger;

@Setter
public class RestTransport implements Transport {

    private static Logger log = Logger.getLogger(RestTransport.class.getSimpleName());

    @Property(source = Constant.REAPER_PROPERTIES, key = "reaper-rest-uri-j-metrics")
    private String reaperJMetricsRestUri = "http://ikube.be:8090/j-metrics";
    @Property(source = Constant.REAPER_PROPERTIES, key = "reaper-rest-uri-o-metrics")
    private String reaperOMetricsRestUri = "http://ikube.be:8090/o-metrics";

    public RestTransport() {
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
