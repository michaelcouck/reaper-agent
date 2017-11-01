package com.pxs.reaper;

import com.pxs.reaper.action.ReaperActionAgentMetrics;
import com.pxs.reaper.action.ReaperActionJmxMetrics;
import com.pxs.reaper.action.ReaperActionOSMetrics;
import com.pxs.reaper.toolkit.FILE;
import com.pxs.reaper.toolkit.THREAD;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.jeasy.props.annotations.Property;

import java.io.File;
import java.lang.management.ManagementFactory;

/**
 * This class sets up the agents running on the local operating system and in the JVMs potentially. Functions that it
 * provides are:
 * <pre>
 *     * Adding th native libraries to the path or Sigar
 *     * Starting the operating system agent
 *     * Attaching to the Java processes
 *     * Detaching from the Java processes when they are terminated
 * </pre>
 * <p>
 * Alternative implementation for attaching to the running java processes on the local machine.
 * <p>
 * <pre>
 *      MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost("localhost");
 *      for (final Integer pid : monitoredHost.activeVms()) {
 *          // Attach here...
 *      }
 * </pre>
 * <p>
 * An alternative for finding the path to the jar. Better implementation, but doesn't work when running from a unit
 * test of course, where the jar is not available
 * <p>
 * <pre>
 *     String pathToAgentJar = ClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
 * </pre>
 */
@Slf4j
@Setter
public class Reaper {

    static {
        Constant.PROPERTIES_INJECTOR.injectProperties(Constant.EXTERNAL_CONSTANTS);
        addNativeLibrariesToPath();
    }

    /**
     * The entry method attaches to the local operating system, attaches to any java processes on the local operating system, and
     * also tries to attach to any process that is exposing jmx on the local machine on the defined port.
     *
     * @param args not used
     */
    public static void main(final String[] args) {
        Reaper reaper = new Reaper();
        reaper.attachToOperatingSystem();
        reaper.attachToJavaProcesses();
        reaper.attachToJmxProcesses();

        // Either sleep for the period specified in the arguments list, or infinitely, almost...
        long waitTime = Long.MAX_VALUE;
        if (args != null && args.length >= 1 && NumberUtils.isNumber(args[0])) {
            waitTime = Long.parseLong(args[0]);
        }
        THREAD.sleep(waitTime);
    }

    /**
     * Adds the native libraries folder to the path and returns the library folder path. Also appends
     * the native library path to the system property {@code Constant.JAVA_LIBRARY_PATH_KEY}.
     *
     * @return the path to the native libraries for all the operating systems
     */
    public static String addNativeLibrariesToPath() {
        String javaLibraryPath = System.getProperty(Constant.JAVA_LIBRARY_PATH_KEY);

        StringBuilder stringBuilder = new StringBuilder(javaLibraryPath);
        File linuxLoadModule = FILE.findFileRecursively(new File("."), Constant.LINUX_LOAD_MODULE);
        if (linuxLoadModule == null) {
            throw new RuntimeException("Native libraries not found, please put 'lib' folder relative to agent start directory");
        }
        File libDirectory = linuxLoadModule.getParentFile();
        stringBuilder.append(File.pathSeparator);
        stringBuilder.append(FILE.cleanFilePath(libDirectory.getAbsolutePath()));
        stringBuilder.append(File.pathSeparator);

        System.setProperty(Constant.JAVA_LIBRARY_PATH_KEY, stringBuilder.toString());

        return libDirectory.getAbsolutePath();
    }

    /**
     * The amount of time to sleep in milliseconds before sampling the operating system and
     * the java processes for telemetry data, and posting to the central analyzer. Note that the
     * average size of a posting is 12 kb, if there are 1000 virtual machines, the network impact
     * will be 1200 kb/second metrics posting.
     */
    @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
    private int sleepTime = 10000;

    private Reaper() {
        Constant.PROPERTIES_INJECTOR.injectProperties(this);
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        log.info("Reaper virtual machine name : " + vmName);
    }

    /**
     * Operating system metrics gathering action.
     */
    void attachToOperatingSystem() {
        // Start the action to gather metrics from the operating system
        ReaperActionOSMetrics reaperActionOSMetrics = new ReaperActionOSMetrics();
        Constant.PROPERTIES_INJECTOR.injectProperties(reaperActionOSMetrics);
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionOSMetrics::terminate));
        Constant.TIMER.scheduleAtFixedRate(reaperActionOSMetrics, sleepTime, sleepTime);
    }

    /**
     * Java process metrics gathering dynamic attache action.
     */
    void attachToJavaProcesses() {
        // Start the action to attach to the Java processes on the local machine and gather metrics from the JVM(s)
        ReaperActionAgentMetrics reaperActionAgentMetrics = new ReaperActionAgentMetrics();
        Constant.PROPERTIES_INJECTOR.injectProperties(reaperActionAgentMetrics);
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionAgentMetrics::terminate));
        Constant.TIMER.scheduleAtFixedRate(reaperActionAgentMetrics, sleepTime, sleepTime);
    }

    /**
     * Java process metrics gathering JMX attach action.
     */
    void attachToJmxProcesses() {
        // Start the action to attach to the Java processes via JMX gather metrics from the JVM(s)
        ReaperActionJmxMetrics reaperActionJmxMetrics = new ReaperActionJmxMetrics();
        Constant.PROPERTIES_INJECTOR.injectProperties(reaperActionJmxMetrics);
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJmxMetrics::terminate));
        Constant.TIMER.scheduleAtFixedRate(reaperActionJmxMetrics, sleepTime, sleepTime);
    }

}