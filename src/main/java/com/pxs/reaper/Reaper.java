package com.pxs.reaper;

import com.jcabi.manifests.Manifests;
import com.pxs.reaper.action.ReaperActionOSMetrics;
import com.pxs.reaper.toolkit.FILE;
import com.pxs.reaper.toolkit.THREAD;
import com.sun.tools.attach.*;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.SigarException;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Reaper {

    public static void main(final String[] args) throws SigarException, IOException {
        new Reaper().reap();
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
        File linuxLoadModule = FILE.findFileRecursively(new File("."), "libsigar-amd64-linux.so");
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

    private List<VirtualMachine> virtualMachines;

    Reaper() throws IOException {
        virtualMachines = new ArrayList<>();

        THREAD.initialize();
        addNativeLibrariesToPath();
        Runtime.getRuntime().addShutdownHook(new Thread(this::detachFromJavaProcesses));
    }

    void reap() {
        // Attach the agent to any Java processes that are running on the local machine
        attachToJavaProcesses();
        // Start the action to gather metrics from the operating system
        new ReaperActionOSMetrics();
    }

    private void detachFromJavaProcesses() {
        virtualMachines.stream().filter(virtualMachine -> virtualMachine != null).forEach(virtualMachine -> {
            try {
                virtualMachine.detach();
            } catch (final IOException e) {
                log.error("Exception detaching from java process : " + virtualMachine, e);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void attachToJavaProcesses() {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        VirtualMachineDescriptor ourOwnDescriptor = getMachineDescriptor(vmName);
        String jarFileName = Manifests.read("Agent-Jar-Name");
        File agentJar = FILE.findFileRecursively(new File("."), jarFileName);
        if (agentJar == null) {
            log.warn("Agent jar not found : ");
        } else {
            String pathToAgentJar = FILE.findFileRecursively(new File("."), "reaper-agent-1.0-SNAPSHOT.jar").getAbsolutePath();
            pathToAgentJar = FILE.cleanFilePath(pathToAgentJar);
            // String pathToAgentJar = ClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            for (final VirtualMachineDescriptor virtualMachineDescriptor : VirtualMachine.list()) {
                if (ourOwnDescriptor.equals(virtualMachineDescriptor)) {
                    // Don't attach to our selves
                    continue;
                }
                VirtualMachine virtualMachine;
                try {
                    virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
                    virtualMachine.loadAgent(pathToAgentJar);
                    virtualMachines.add(virtualMachine);
                    log.error("Attached to running java process : " + virtualMachineDescriptor);
                } catch (final AttachNotSupportedException | IOException | AgentInitializationException | AgentLoadException e) {
                    log.error("Exception attaching to java process : " + virtualMachineDescriptor, e);
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

}