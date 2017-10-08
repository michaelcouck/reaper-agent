package com.pxs.reaper.action;

import com.pxs.reaper.model.Metrics;
import com.pxs.reaper.toolkit.FILE;
import com.sun.tools.attach.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 *
 */
@Slf4j
@Setter
public class ReaperActionJvmMetrics implements ReaperAction, Runnable {

    @SuppressWarnings("unused")
    private volatile Metrics metrics;

    @SuppressWarnings("ConstantConditions")
    public ReaperActionJvmMetrics() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        VirtualMachineDescriptor ourOwnDescriptor = getMachineDescriptor(vmName);
        String pathToAgentJar = FILE.findFileRecursively(new File("."), "serenity.jar").getAbsolutePath();
        pathToAgentJar = FILE.cleanFilePath(pathToAgentJar);
        // String pathToAgentJar = ClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        for (final VirtualMachineDescriptor virtualMachineDescriptor : VirtualMachine.list()) {
            if (ourOwnDescriptor.equals(virtualMachineDescriptor)) {
                // Don't attach to our selves
                continue;
            }
            VirtualMachine virtualMachine = null;
            try {
                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
                virtualMachine.loadAgent(pathToAgentJar);
                log.error("Attached to running VM : " + virtualMachineDescriptor);
            } catch (final AttachNotSupportedException | IOException | AgentInitializationException | AgentLoadException e) {
                log.error("Exception attaching to JVM : " + virtualMachineDescriptor, e);
            } finally {
                if (virtualMachine != null) {
                    try {
                        virtualMachine.detach();
                    } catch (final IOException e) {
                        log.error("Exception detaching from java process : " + virtualMachineDescriptor, e);
                    }
                }
            }
        }
    }

    private VirtualMachineDescriptor getMachineDescriptor(final String vmName) {
        for (final VirtualMachineDescriptor virtualMachineDescriptor : VirtualMachine.list()) {
            String id = virtualMachineDescriptor.id();
            if (vmName.contains(id)) {
                return virtualMachineDescriptor;
            }
        }
        throw new RuntimeException("No virtual machine descriptor, are we on Solaris : ");
    }

    @Override
    public void run() {
        // TODO: Collect the metrics and java stack/method data here
    }

}