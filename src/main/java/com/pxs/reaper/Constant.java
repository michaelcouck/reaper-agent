package com.pxs.reaper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pxs.reaper.action.ReaperAgent;
import com.pxs.reaper.toolkit.FILE;
import com.pxs.reaper.transport.RestTransport;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.PropertiesInjector;

import java.io.File;
import java.io.InputStream;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

/**
 * Holds constant variables and utility objects that many classes in the agent require.
 *
 * @author Michael Couck
 * @version 1.1
 * @since 20-10-2017
 */
public interface Constant {

    Logger LOG = Logger.getLogger(Constant.class.getSimpleName());

    String REAPER_PROPERTIES_FILE = "reaper-application.properties";
    /**
     * Properties file for various parameterization. Used in conjunction with {@link Property} annotations is quite convenient
     */
    String REAPER_PROPERTIES = "file:./" + REAPER_PROPERTIES_FILE;
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
    @SuppressWarnings("unused")
    Transport TRANSPORT_WEB_SOCKET = new WebSocketTransport();
    Transport TRANSPORT = TRANSPORT_REST;

    long SLEEP_TIME = EXTERNAL_CONSTANTS.getSleepTime();

    @Getter
    @Setter
    class ExternalConstants {

        {
            getProperties();
            PROPERTIES_INJECTOR.injectProperties(this);
            LOG.log(Level.INFO, "Sleep time set to : {0}", new Object[]{getSleepTime()});
        }

        /**
         * The amount of time to sleep in milliseconds before sampling the operating system and
         * the java processes for telemetry data, and posting to the central analyzer. Note that the
         * average size of a posting is 12 kb, if there are 1000 virtual machines, the network impact
         * will be 1200 kb/second metrics posting.
         */
        @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
        private int sleepTime = 15000;

        void getProperties() {
            // If there is no properties file outside the jar, then extract the application properties from
            // the jar and write it to the file system outside the jar for external modification and configuration
            File propertiesFile = new File(Constant.REAPER_PROPERTIES_FILE);
            if (!propertiesFile.exists()) {
                LOG.log(Level.INFO, "Writing properties file in jar to directory : ");
                InputStream propertiesInputStream = null;
                try {
                    propertiesInputStream = ReaperAgent.class.getClassLoader().getResourceAsStream(Constant.REAPER_PROPERTIES_FILE);
                    String content = FILE.getContents(propertiesInputStream, Integer.MAX_VALUE).toString();
                    propertiesFile = FILE.getOrCreateFile(new File(Constant.REAPER_PROPERTIES_FILE));
                    FILE.setContents(propertiesFile, content.getBytes());
                } finally {
                    LOG.log(
                            Level.INFO,
                            "Wrote properties file in jar to directory, input stream : {0}, properties file : {1}",
                            new Object[]{propertiesInputStream, propertiesFile});
                    FILE.close(propertiesInputStream);
                }
            } else {
                LOG.log(Level.INFO, "Found properties file in directory : {0}", new Object[]{propertiesFile});
            }
        }
    }
}