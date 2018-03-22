package com.pxs.reaper.transport;

import com.mashape.unirest.http.Unirest;
import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.model.OSMetrics;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.annotations.Property;

import java.util.logging.Level;
import java.util.logging.Logger;

@Setter
@Getter
public class RestTransport implements Transport {

    private static Logger log = Logger.getLogger(RestTransport.class.getSimpleName());

    @Property(key = Constant.REST_URI_J_METRICS, source = Constant.REAPER_PROPERTIES)
    private String reaperJMetricsRestUri;
    @Property(key = Constant.REST_URI_O_METRICS, source = Constant.REAPER_PROPERTIES)
    private String reaperOMetricsRestUri;

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
                log.log(Level.WARNING, "No endpoint for object : ", new Object[]{metrics});
            }
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Error posting to micro service, is it running?", e);
        }
        return true;
    }

}