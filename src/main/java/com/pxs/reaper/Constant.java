package com.pxs.reaper;

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
 * @version 1.0
 * @since 20-10-2017
 */
public interface Constant {

    /**
     * Properties file for various parameterization. Used in conjunction with {@link Property} annotations is quite convenient
     */
    String REAPER_PROPERTIES = "reaper.properties";
    /**
     * The $PATH variable in the Java process for linking the native libraries to
     */
    String JAVA_LIBRARY_PATH_KEY = "java.library.path";
    /**
     * The linux sigar load module, to find where in the folder structure these link libraries are and add them to the $PATH
     */
    String LINUX_LOAD_MODULE = "libsigar-amd64-linux.so";

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

    @Getter
    @Setter
    class ExternalConstants {
        @SuppressWarnings("unused")
        @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
        private int sleepTime = 10000;
    }

}