package com.pxs.reaper.agent.transport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.model.NetMetrics;
import com.pxs.reaper.agent.model.OSMetrics;
import lombok.Getter;
import lombok.Setter;
import org.hyperic.sigar.NetConnection;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.annotations.Property;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transport to elastic rather than couchbase.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2019
 */
@Setter
@Getter
public class ElasticTransport implements Transport {

    private static Logger log = Logger.getLogger(ElasticTransport.class.getSimpleName());
    private static String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    private Gson gson;

    @Property(key = Constant.ELASTIC_URI, source = Constant.REAPER_PROPERTIES)
    private String elasticUri;

    public ElasticTransport() {
        PropertiesInjectorBuilder.aNewPropertiesInjector().injectProperties(this);
        gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat(dateFormat).create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean postMetrics(final Object metrics) {
        try {
            if (!OSMetrics.class.isAssignableFrom(metrics.getClass())) {
                return Boolean.FALSE;
            }
            OSMetrics osMetrics = (OSMetrics) metrics;
            NetMetrics netMetrics = new NetMetrics();
            for (final NetConnection netConnection : osMetrics.getNetConnections()) {
                String localAddress = osMetrics.getIpAddress();
                String remoteAddress = netConnection.getRemoteAddress();
                netMetrics.setLocalAddress(localAddress);
                netMetrics.setRemoteAddress(remoteAddress);
                post(netMetrics);
                netMetrics.setLocalAddress(remoteAddress);
                netMetrics.setRemoteAddress(localAddress);
                post(netMetrics);
            }
            return Boolean.TRUE;
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Error posting to micro service, is it running... : ");
            log.log(Level.FINE, "                   stack trace for network error : ");
            return Boolean.FALSE;
        }
    }

    private void post(final NetMetrics netMetrics) throws UnirestException {
        String uid = UUID.randomUUID().toString();
        String json = new Gson().toJson(netMetrics);
        HttpResponse httpResponse = Unirest.post(elasticUri + uid).header("Content-Type", "application/json").body(json).asString();
        if (httpResponse != null && httpResponse.getStatus() < 200 && httpResponse.getStatus() < 300) {
            log.log(Level.WARNING, "Posting metrics was not successful : " + httpResponse.getStatus());
        }
    }

}