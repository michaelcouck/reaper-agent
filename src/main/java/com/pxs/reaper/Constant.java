package com.pxs.reaper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pxs.reaper.transport.RestTransport;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.PropertiesInjector;

import java.util.Timer;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

/**
 * Holds constant variables and utility objects that many classes in the agent require.
 *
 * @author Michael Couck
 * @version 1.1
 * @since 20-10-2017
 */
public interface Constant {

    /**
     * Properties file for various parameterization. Used in conjunction with {@link Property} annotations is quite convenient
     */
    String REAPER_PROPERTIES = "application.properties";
    /**
     * The $PATH variable in the Java process for linking the native libraries to
     */
    String JAVA_LIBRARY_PATH_KEY = "java.library.path";
    /**
     * The linux sigar load module, to find where in the folder structure these link libraries are and add them to the $PATH
     */
    String LINUX_LOAD_MODULE = "libsigar-amd64-linux.so";
    /**
     * The date format for the serializer.
     */
    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * The Json serializer and de serializer.
     */
    Gson GSON = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

    /**
     * The timer for all the actions, {@link com.pxs.reaper.action.ReaperActionAgentMetrics}, {@link com.pxs.reaper.action.ReaperActionJmxMetrics} etc.
     */
    Timer TIMER = new Timer(true);
    /**
     * This class opens the {@link Constant#REAPER_PROPERTIES} file, looks through all the {@link Property} annotations, then calls
     * the setter methods for the properties in the target objects. Like Spring, but simpler.
     */
    PropertiesInjector PROPERTIES_INJECTOR = aNewPropertiesInjector();

    /**
     * Required to populate these properties in a no static class rather than interfere with the {@link com.pxs.reaper.action.ReaperAgent}
     */
    ExternalConstants EXTERNAL_CONSTANTS = new ExternalConstants();

    String WEB_SOCKET_URI = "reaper-web-socket-uri";
    String LOCAL_JMX_URI = "localhost-jmx-uri";
    String REST_URI_J_METRICS = "reaper-rest-uri-j-metrics";
    String REST_URI_O_METRICS = "reaper-rest-uri-o-metrics";

    /**
     * Provides TRANSPORT of the metrics from the class to the central analyzer over the wire
     */
    Transport TRANSPORT_REST = new RestTransport();
    Transport TRANSPORT_WEB_SOCKET = new WebSocketTransport();
    Transport TRANSPORT = TRANSPORT_REST;

    default Transport getTransport() {
        return new RestTransport();
    }

    @Getter
    @Setter
    class ExternalConstants {

        {
            PROPERTIES_INJECTOR.injectProperties(this);
        }

        @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
        private int sleepTime = 15000;
    }

}