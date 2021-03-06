package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.toolkit.OS;
import com.pxs.reaper.agent.toolkit.MANIFEST;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.lang3.StringUtils;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class iterates through pids on the local operating system, and tries to attach Java agents to them. When JAva processes
 * terminate the agent will be removed from the list, and similarly agents will not be attached twice to the same Java process. The
 * same agent will be attached to the Java process where the agent is running, publishing local telemetry and metrics.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@SuppressWarnings("WeakerAccess")
public class ReaperActionAgentMetrics extends TimerTask implements ReaperAction {

    private final Logger log = Logger.getLogger(this.getClass().getSimpleName());

    /**
     * The path to the agent jar, which is needed to attach to the Java processes
     */
    private final String pathToAgent;
    /**
     * The map of {@link VirtualMachine}s, keyed by the pid on the local machine
     */
    private final Map<String, VirtualMachine> virtualMachines;
    /**
     * List of pid(s) that we couldn't connect to, so we don't keep throwing exceptions
     */
    private List<String> virtualMachineErrorPids;

    public ReaperActionAgentMetrics() {
        pathToAgent = MANIFEST.getPathToAgent();
        virtualMachines = new HashMap<>();
        virtualMachineErrorPids = new LinkedList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        attachToJavaProcesses();
    }

    /**
     * Iterates through the list of pids on the local operating system and tries to attach the agent to them.
     */
    void attachToJavaProcesses() {
        // First remove all the virtual machines that have terminated
        removeTerminatedProcesses();
        // Get the pids for the local operating system
        Set<String> pids = getPidsFromOperatingSystem();
        log.fine("Pids : " + pids);
        for (final String pid : pids) {
            VirtualMachine virtualMachine = null;
            try {
                if (virtualMachines.containsKey(pid) || virtualMachineErrorPids.contains(pid)) {
                    log.fine("Already attached/tried to attach to : " + pid);
                    continue;
                }
                virtualMachineErrorPids.add(pid);
                if (OS.isOs("Windows")) {
                    System.loadLibrary("attach");
                }
                // String manifestClassPath = MANIFEST.getAgentClassPath();
                // log.fine("Java class path passed to attached agent : " + manifestClassPath);
                if (StringUtils.isNotEmpty(pathToAgent)) {
                    virtualMachine = VirtualMachine.attach(pid);
                    // log.info("Agent properties : " + virtualMachine.getAgentProperties());
                    // log.info("System properties : " + virtualMachine.getSystemProperties());

                    // This doesn't seem to work on linux????!!! OMG WTF?!
                    // HotSpotVirtualMachine#loadAgent(String, String)
                    // virtualMachine.loadAgent(pathToAgent, "-Dclass-path=" + manifestClassPath);
                    // virtualMachine.loadAgentLibrary(pathToAgent, manifestClassPath);
                    // virtualMachine.loadAgentPath(pathToAgent, manifestClassPath);

                    virtualMachine.loadAgent(pathToAgent);
                } else {
                    log.warning("Agent jar not found : ");
                }
                virtualMachines.put(pid, virtualMachine);
            } catch (final AttachNotSupportedException | IOException | AgentLoadException | AgentInitializationException e) {
                log.log(Level.SEVERE, "Exception attaching to pid : " + pid, e);
            }
        }
    }

    /**
     * Detaches from all Java processes that are currently attached by this agent.
     */
    void detachFromJavaProcesses() {
        Collection<VirtualMachine> machines = virtualMachines.values();
        machines.forEach(virtualMachine -> {
            try {
                log.info("Detaching from : " + virtualMachine.id() + ":" + virtualMachine.getClass().getName());
                virtualMachine.detach();
            } catch (final IOException e) {
                log.log(Level.SEVERE, "Exception detaching from java process : " + virtualMachine.id(), e);
            }
        });
        Set<String> pids = new TreeSet<>(virtualMachines.keySet());
        pids.forEach(virtualMachines::remove);
    }

    /**
     * All the pids from the local operating system. Pids are re-cycled, typically starting at 1024 and going up to +-65000, consequently
     * there is very little chance of the same pid being used twice and clashing with an existing pid that is key to the virtual machine that
     * the agent is attached to/
     *
     * @return the pids from the local operating system in string format, originally in integer format
     */
    private Set<String> getPidsFromOperatingSystem() {
        MonitoredHost monitoredHost;
        try {
            monitoredHost = MonitoredHost.getMonitoredHost("localhost");
            Set<Integer> vmIdentifiers = monitoredHost.activeVms();
            log.fine("Active JVM identifiers : " + vmIdentifiers);
            return monitoredHost.activeVms().stream().map(Object::toString).collect(Collectors.toSet());
        } catch (final MonitorException | URISyntaxException e) {
            log.log(Level.SEVERE, "Exception getting the pids from the OS : ", e);
            //noinspection unchecked
            return Collections.EMPTY_SET;
        }
    }

    /**
     * Removes and optionally detaches the agents from terminated processes. Java processes might become
     * defunct, in which case they remain 'alive', the {@link VirtualMachine#detach()} will in that case remove the
     * agent from the process.
     */
    private void removeTerminatedProcesses() {
        // Pids from the OS
        Set<String> pids = getPidsFromOperatingSystem();
        // All the pids that we have attached to
        Set<String> terminatedPids = new TreeSet<>(virtualMachines.keySet());
        // Reduce the set by removing the current OS pids from VM pids
        terminatedPids.removeAll(pids);

        // What is left is all the VMs that we have attached to that have terminated

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
                    log.log(Level.SEVERE, "Exception detaching from process : " + pid, e);
                }
            }
        });
        // And remove the references from the map tpo be scavenged
        virtualMachines.keySet().removeAll(terminatedPids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean terminate() {
        detachFromJavaProcesses();
        return cancel();
    }
}