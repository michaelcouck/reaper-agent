package com.pxs.reaper.agent;

import org.jeasy.props.annotations.Property;

/**
 * Note to self, don't put anything in here that is not {@link java}, i.e. nothing
 * that is not from rt.jar/tools.jar, and certainly nothing functional, only scalar
 * instance variables.
 * <p>
 * Holds constant variables and utility objects that many classes in the agent require.
 *
 * @author Michael Couck
 * @version 1.1
 * @since 20-10-2017
 */
public interface Constant {
    /**
     * The name of the properties file, should be relatively unique to avoid name clashing.
     */
    String REAPER_PROPERTIES_FILE = "reaper-application.properties";
    /**
     * Properties file for various parameterization. Used in conjunction with {@link Property} annotations is quite convenient
     */
    String REAPER_PROPERTIES = /* "file:./" + */ REAPER_PROPERTIES_FILE;
    /**
     * The $PATH variable in the Java process for linking the native libraries to
     */
    String JAVA_LIBRARY_PATH_KEY = "java.library.path";
    /**
     * The linux sigar load module, to find where in the folder structure these link libraries are and add them to the $PATH
     */
    String LINUX_LOAD_MODULE = "libsigar-amd64-linux.so";
    /**
     * Properties for the transport, web socket, the rest endpoints etc.
     */
    String LOCAL_JMX_URI = "localhost-jmx-uri";
    String WEB_SOCKET_URI = "reaper-web-socket-uri";
    String REST_URI_J_METRICS = "reaper-rest-uri-j-metrics";
    String REST_URI_O_METRICS = "reaper-rest-uri-o-metrics";
    /**
     * Sleep time hard coded, no way to parameterize(make dynamic) this on all platforms.
     */
    long SLEEP_TIME = 3000;

}