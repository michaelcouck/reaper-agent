package com.pxs.reaper.agent.transport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.model.JMetrics;
import com.pxs.reaper.agent.model.OSMetrics;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.annotations.Property;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The representational state transfer class to post the metrics from the agent in the target virtual
 * machine to the central controller, i.e. the micro service that will generate the models from the metrics
 * data.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-03-2018
 */
@Setter
@Getter
public class RestTransport implements Transport {

    private static Logger log = Logger.getLogger(RestTransport.class.getSimpleName());
    private static String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    private Gson gson;

    @Property(key = Constant.REST_URI_J_METRICS, source = Constant.REAPER_PROPERTIES)
    private String reaperJMetricsRestUri;
    @Property(key = Constant.REST_URI_O_METRICS, source = Constant.REAPER_PROPERTIES)
    private String reaperOMetricsRestUri;

    public RestTransport() {
        System.out.println("System class loader: " + ClassLoader.getSystemClassLoader());
        System.out.println("Context class loader: " + Thread.currentThread().getContextClassLoader());
        System.out.println("This class loader: " + this.getClass().getClassLoader());
        System.out.println("Transport class loader: " + RestTransport.class.getClassLoader());
        System.out.println("Properties injector class loader : " + PropertiesInjectorBuilder.class.getClassLoader());
        System.out.println("Gson class loader : " + Gson.class.getClassLoader());
        PropertiesInjectorBuilder.aNewPropertiesInjector().injectProperties(this);
        gson = new GsonBuilder().setDateFormat(dateFormat).create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postMetrics(final Object metrics) {
        try {
            String json = gson.toJson(metrics);
            HttpResponse httpResponse = null;
            if (JMetrics.class.isAssignableFrom(metrics.getClass())) {
                httpResponse = Unirest.post(reaperJMetricsRestUri).body(json).asString();
            } else if (OSMetrics.class.isAssignableFrom(metrics.getClass())) {
                httpResponse = Unirest.post(reaperOMetricsRestUri).body(json).asString();
            } else {
                log.log(Level.WARNING, "No endpoint for object : ", new Object[]{metrics});
            }
            if (httpResponse != null && httpResponse.getStatus() != 200) {
                log.log(Level.WARNING, "Posting metrics was not successful : " + httpResponse);
            }
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Error posting to micro service, is it running?", e);
        }
        return true;
    }

}