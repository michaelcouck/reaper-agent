package com.pxs.reaper.agent;

import com.pxs.reaper.agent.action.ReaperActionAgentMetrics;
import com.pxs.reaper.agent.action.ReaperActionJmxMetrics;
import com.pxs.reaper.agent.action.ReaperActionOSMetrics;
import com.pxs.reaper.agent.action.ReaperAgent;
import com.pxs.reaper.agent.toolkit.FILE;
import com.pxs.reaper.agent.toolkit.THREAD;
import org.apache.commons.lang3.math.NumberUtils;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.api.PropertiesInjector;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class Reaper {

    static {
        addNativeLibrariesToPath();
    }

    /**
     * The entry method attaches to the local operating system, attaches to any java processes on the local operating system, and
     * also tries to attach to any process that is exposing jmx on the local machine on the defined port.
     *
     * @param args could contain the amount of time to sleep for in first argument, if not we run infinitely
     */
    public static void main(final String[] args) {
        // We don't attach to our selves
        ReaperAgent.LOADED.set(Boolean.TRUE);

        Reaper reaper = new Reaper();
        reaper.attachToOperatingSystem();
        reaper.attachToJavaProcesses();
        // Either sleep for the period specified in the arguments list, or infinitely, almost...
        //noinspection deprecation
        if (args != null && args.length >= 1 && NumberUtils.isNumber(args[0])) {
            long waitTime = Long.parseLong(args[0]);
            THREAD.sleep(waitTime);
        } else {
            //noinspection InfiniteLoopStatement
            while (true) {
                THREAD.sleep(Long.MAX_VALUE);
            }
        }
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

    private static Logger log = Logger.getLogger(Reaper.class.getSimpleName());

    private PropertiesInjector propertiesInjector;

    Reaper() {
        propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        log.log(Level.FINEST, "Reaper virtual machine name : " + vmName);
    }

    /**
     * Operating system metrics gathering action.
     */
    void attachToOperatingSystem() {
        // Start the action to gather metrics from the operating system
        ReaperActionOSMetrics reaperActionOSMetrics = new ReaperActionOSMetrics();
        propertiesInjector.injectProperties(reaperActionOSMetrics);
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionOSMetrics::terminate));
        THREAD.scheduleAtFixedRate(reaperActionOSMetrics, Constant.WAIT_TO_POST_METRICS, Constant.WAIT_TO_POST_METRICS);
    }

    /**
     * Java process metrics gathering dynamic attache action.
     */
    void attachToJavaProcesses() {
        // Start the action to attach to the Java processes on the local machine and gather metrics from the JVM(s)
        ReaperActionAgentMetrics reaperActionAgentMetrics = new ReaperActionAgentMetrics();
        propertiesInjector.injectProperties(reaperActionAgentMetrics);
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionAgentMetrics::terminate));
        THREAD.scheduleAtFixedRate(reaperActionAgentMetrics, Constant.WAIT_TO_ATTACH_FOR, Constant.WAIT_TO_ATTACH_FOR);
    }

    /**
     * Java process metrics gathering JMX attach action.
     */
    @SuppressWarnings("deprecation")
    void attachToJmxProcesses() {
        // Start the action to attach to the Java processes via JMX gather metrics from the JVM(s)
        ReaperActionJmxMetrics reaperActionJmxMetrics = new ReaperActionJmxMetrics();
        propertiesInjector.injectProperties(reaperActionJmxMetrics);
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionJmxMetrics::terminate));
        THREAD.scheduleAtFixedRate(reaperActionJmxMetrics, Constant.WAIT_TO_ATTACH_FOR, Constant.WAIT_TO_ATTACH_FOR);
    }

}