package com.pxs.reaper;

import com.jcabi.manifests.Manifests;
import com.pxs.reaper.action.ReaperActionOSMetrics;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
public class Reaper {

    /* Initialize the executor service */
    static {
        THREAD.initialize();
        Constant.PROPERTIES_INJECTOR.injectProperties(Constant.EXTERNAL_CONSTANTS);
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) {
        // Start a schedule that will continuously check the pids and
        // either attach to the new ones or detach from the terminated ones
        Future<?> future = THREAD.submit("reaper-thread", () -> {
            Reaper reaper = new Reaper();
            reaper.attachToOperatingSystem();
            while (true) {
                try {
                    reaper.attachToJavaProcesses();
                } catch (final Exception e) {
                    log.error("Exception attaching to the JVMs : ", e);
                } finally {
                    THREAD.sleep(60000);
                }
            }
        });
        long waitTime = Long.MAX_VALUE;
        if (args != null && args.length >= 1 && NumberUtils.isNumber(args[0])) {
            waitTime = Long.parseLong(args[0]);
        }
        THREAD.waitForFuture(future, waitTime);
    }

    /**
     * Adds the native libraries folder to the path and returns the library folder path. Also appends
     * the native library path to the system property {@code Constant.JAVA_LIBRARY_PATH_KEY}.
     *
     * @return the path to the native libraries for all the operating systems
     */
    @SuppressWarnings("WeakerAccess")
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

    private String pathToAgent;
    private final Map<String, VirtualMachine> virtualMachines;

    Reaper() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        log.info("Reaper virtual machine name : " + vmName);

        addNativeLibrariesToPath();
        pathToAgent = getPathToAgent();
        virtualMachines = new HashMap<>();
        Runtime.getRuntime().addShutdownHook(new Thread(this::detachFromJavaProcesses));
    }

    void attachToOperatingSystem() {
        // Start the action to gather metrics from the operating system
        int sleepTime = Constant.EXTERNAL_CONSTANTS.getSleepTime();
        ReaperActionOSMetrics reaperActionOSMetrics = new ReaperActionOSMetrics();
        Runtime.getRuntime().addShutdownHook(new Thread(reaperActionOSMetrics::terminate));
        Constant.TIMER.scheduleAtFixedRate(reaperActionOSMetrics, sleepTime, sleepTime);
    }

    void attachToJavaProcesses() {
        // First remove all the virtual machines that have terminated
        removeTerminatedProcesses();

        Set<String> pids = getPidsFromOperatingSystem();
        for (final String pid : pids) {
            VirtualMachine virtualMachine;
            try {
                if (virtualMachines.containsKey(pid)) {
                    log.debug("Already attached to : {}", pid);
                    continue;
                }
                virtualMachine = VirtualMachine.attach(pid);
                if (StringUtils.isNotEmpty(pathToAgent)) {
                    virtualMachine.loadAgent(pathToAgent);
                } else {
                    log.warn("Agent jar not found : ");
                }
                virtualMachines.put(pid, virtualMachine);
                log.info("Attached to pid : {}, {}", pid, virtualMachine.getClass().getName());
            } catch (final AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException e) {
                log.error("Exception attaching to pid : " + pid, e);
            }
        }
    }

    void detachFromJavaProcesses() {
        Collection<VirtualMachine> machines = virtualMachines.values();
        machines.forEach(virtualMachine -> {
            try {
                log.info("Detaching from : {}, {}", virtualMachine.id(), virtualMachine.getClass().getName());
                virtualMachine.detach();
            } catch (final IOException e) {
                log.error("Exception detaching from java process : " + virtualMachine.id(), e);
            }
        });
        Set<String> pids = new TreeSet<>(virtualMachines.keySet());
        pids.forEach(virtualMachines::remove);
    }

    private Set<String> getPidsFromOperatingSystem() {
        MonitoredHost monitoredHost;
        try {
            monitoredHost = MonitoredHost.getMonitoredHost("localhost");
            return monitoredHost.activeVms().stream().map(Object::toString).collect(Collectors.toSet());
        } catch (final MonitorException | URISyntaxException e) {
            log.error("Exception getting the pids from the OS : ", e);
            //noinspection unchecked
            return Collections.EMPTY_SET;
        }
    }

    private String getPathToAgent() {
        String jarFileName = Manifests.read("Agent-Jar-Name");
        File agentJar = FILE.findFileRecursively(new File("."), jarFileName);
        return (agentJar != null) ? FILE.cleanFilePath(agentJar.getAbsolutePath()) : null;
    }

    private void removeTerminatedProcesses() {
        Set<String> pids = getPidsFromOperatingSystem();
        Set<String> terminatedPids = new TreeSet<>(virtualMachines.keySet());
        // Remove all the active pids from the attached pids and you have the terminated pids left over
        terminatedPids.removeAll(pids);

        // Remove all the pids in the currently attached map that are not
        // found in the pid list from the operating system, i.e. the process has
        // been terminated at operating system level
        terminatedPids.forEach(pid -> {
            log.info("Removing terminated pid : " + pid);
            VirtualMachine virtualMachine = virtualMachines.get(pid);
            if (virtualMachine != null) {
                try {
                    virtualMachine.detach();
                } catch (final Exception e) {
                    log.error("Exception detaching from process : {}", pid, e);
                }
            }
        });
        virtualMachines.keySet().removeAll(terminatedPids);
    }

}