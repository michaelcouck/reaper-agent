package com.pxs.reaper;

import org.jeasy.props.api.PropertiesInjector;

import java.util.Timer;

import static org.jeasy.props.PropertiesInjectorBuilder.aNewPropertiesInjector;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
public interface Constant {

    Timer TIMER = new Timer(true);

    String REAPER_PROPERTIES = "reaper.properties";
    String JAVA_LIBRARY_PATH_KEY = "java.library.path";

    PropertiesInjector PROPERTIES_INJECTOR = aNewPropertiesInjector();

    String LINUX_LOAD_MODULE = "libsigar-amd64-linux.so";
}