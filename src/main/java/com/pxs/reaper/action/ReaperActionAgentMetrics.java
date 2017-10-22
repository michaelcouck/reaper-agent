package com.pxs.reaper.action;

import com.jcabi.manifests.Manifests;
import com.pxs.reaper.Constant;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import ikube.toolkit.FILE;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("WeakerAccess")
public class ReaperActionAgentMetrics extends TimerTask implements ReaperAction {

    private String pathToAgent;
    private final Map<String, VirtualMachine> virtualMachines;

    public ReaperActionAgentMetrics() {
        pathToAgent = getPathToAgent();
        virtualMachines = new HashMap<>();
    }

    @Override
    public void run() {
        attachToJavaProcesses();
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

    @Override
    public boolean terminate() {
        detachFromJavaProcesses();
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }
}