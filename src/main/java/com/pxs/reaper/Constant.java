package com.pxs.reaper;

import lombok.Getter;
import lombok.Setter;
import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.PropertiesInjector;

import java.util.Timer;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
public interface Constant {

    String REAPER_PROPERTIES = "reaper.properties";
    String JAVA_LIBRARY_PATH_KEY = "java.library.path";
    String LINUX_LOAD_MODULE = "libsigar-amd64-linux.so";

    Timer TIMER = new Timer(true);
    PropertiesInjector PROPERTIES_INJECTOR = aNewPropertiesInjector();

    ExternalConstants EXTERNAL_CONSTANTS = new ExternalConstants();
    @Getter
    @Setter
    class ExternalConstants {
        @SuppressWarnings("unused")
        @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
        private int sleepTime = 10;
    }

}