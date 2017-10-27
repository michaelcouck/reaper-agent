package com.pxs.reaper.action;

import com.jcabi.manifests.Manifests;
import com.pxs.reaper.Constant;
import com.pxs.reaper.toolkit.FILE;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
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
@Slf4j
@SuppressWarnings("WeakerAccess")
public class ReaperActionAgentMetrics extends TimerTask implements ReaperAction {

    /**
     * The path to the agent jar, which is needed to attach to the Java processes
     */
    private final String pathToAgent = getPathToAgent();
    /**
     * The map of {@link VirtualMachine}s, keyed by the pid on the local machine
     */
    private final Map<String, VirtualMachine> virtualMachines = new HashMap<>();

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

    /**
     * Detaches from all Java processes that are currently attached by this agent.
     */
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
            return monitoredHost.activeVms().stream().map(Object::toString).collect(Collectors.toSet());
        } catch (final MonitorException | URISyntaxException e) {
            log.error("Exception getting the pids from the OS : ", e);
            //noinspection unchecked
            return Collections.EMPTY_SET;
        }
    }

    /**
     * Access to the jar that contains the {@link ReaperAgent}. The path is necessary to attach dynamically to Java processes.
     *
     * @return the absolute, cleaned, normalized, canonical path to the jar containing 'this' Java agent
     */
    private String getPathToAgent() {
        String jarFileName = Manifests.read("Agent-Jar-Name");
        File agentJar = FILE.findFileRecursively(new File("."), jarFileName);
        return (agentJar != null) ? FILE.cleanFilePath(agentJar.getAbsolutePath()) : null;
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
                    log.error("Exception detaching from process : {}", pid, e);
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
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }
}