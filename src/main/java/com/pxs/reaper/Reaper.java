package com.pxs.reaper;

import com.jcabi.manifests.Manifests;
import com.pxs.reaper.action.ReaperActionOSMetrics;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Alternative implementation for attaching to the running java processes on the local machine.
 * <p>
 * <pre>
 *      MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost("localhost");
 *          for (final Integer pid : monitoredHost.activeVms()) {
 *              VirtualMachine virtualMachine;
 *              try {
 *                  // virtualMachine = new LinuxVirtualMachine(ATTACH_PROVIDER, pid);
 *                  virtualMachine = VirtualMachine.attach(String.valueOf(pid));
 *                  virtualMachine.loadAgent(pathToAgentJar);
 *                  virtualMachines.put(pid, virtualMachine);
 *                  log.error("Attached to running java process : {}, {}", pid, virtualMachine.id());
 *              } catch (final Exception e) {
 *              log.error("Exception attaching to java process : " + pid, e);
 *          }
 *      }
 * </pre>
 */
@Slf4j
public class Reaper {

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(final String[] args) throws Exception {
        THREAD.initialize();
        final Reaper reaper = new Reaper();
        Future<?> future = THREAD.submit("reaper-thread", () -> {
            reaper.attachToOperatingSystem();
            while (true) {
                try {
                    reaper.attachToJavaProcesses();
                } catch (final Exception e) {
                    log.error("Exception attaching to the JVMs : ", e);
                } finally {
                    THREAD.sleep(60000);
                }
                // TODO: If we are in a test then terminate
                // TODO: Send data on the reaper agent
            }
        });
        THREAD.waitForFuture(future, Long.MAX_VALUE);
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

    private Map<String, VirtualMachine> virtualMachines;

    Reaper() throws IOException {
        virtualMachines = new HashMap<>();
        addNativeLibrariesToPath();
        Runtime.getRuntime().addShutdownHook(new Thread(this::detachFromJavaProcesses));
    }

    void attachToOperatingSystem() {
        // Start the action to gather metrics from the operating system
        new ReaperActionOSMetrics();
    }

    private void detachFromJavaProcesses() {
        virtualMachines.values().stream().filter(virtualMachine -> virtualMachine != null).forEach(virtualMachine -> {
            try {
                log.info("Detaching from : {}", virtualMachine);
                virtualMachine.detach();
            } catch (final Exception e) {
                log.error("Exception detaching from java process : " + virtualMachine, e);
            }
        });
    }

    void attachToJavaProcesses() throws Exception {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        log.info("My name is : " + vmName);
        VirtualMachineDescriptor ourOwnDescriptor = getMachineDescriptor(vmName);

        String jarFileName = Manifests.read("Agent-Jar-Name");
        File agentJar = FILE.findFileRecursively(new File("."), jarFileName);
        if (agentJar == null) {
            log.warn("Agent jar not found : ");
        } else {
            String pathToAgentJar = FILE.cleanFilePath(agentJar.getAbsolutePath());
            // String pathToAgentJar = ClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            log.debug("Virtual machines : {}", VirtualMachine.list());

            // First remove all the virtual machines that have terminated
            Set<String> virtualMachineDescriptors = virtualMachines.keySet();
            Object[] virtualMachineDescriptorArray = virtualMachineDescriptors.toArray(new Object[virtualMachineDescriptors.size()]);
            for (final Object virtualMachineDescriptor : virtualMachineDescriptorArray) {
                //noinspection SuspiciousMethodCalls
                if (VirtualMachine.list().contains(virtualMachineDescriptor)) {
                    //noinspection SuspiciousMethodCalls
                    VirtualMachine virtualMachine = virtualMachines.remove(virtualMachineDescriptor);
                    log.info("Removed virtual machine : {}", virtualMachine);
                }
            }

            for (final VirtualMachineDescriptor virtualMachineDescriptor : VirtualMachine.list()) {
                //noinspection StatementWithEmptyBody
                if (virtualMachineDescriptor.equals(ourOwnDescriptor)) {
                    // Don't attach to our selves
                    // continue;
                    // Actually, do attach to our selves...
                }
                // Check that we don't already have this vm
                if (virtualMachines.containsKey(virtualMachineDescriptor.displayName())) {
                    log.debug("Already attached to : {}", virtualMachineDescriptor.displayName());
                    continue;
                }
                VirtualMachine virtualMachine;
                try {
                    virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
                    virtualMachine.loadAgent(pathToAgentJar);
                    virtualMachines.put(virtualMachineDescriptor.displayName(), virtualMachine);
                    log.info("Attached to running java process : {}, {}", virtualMachineDescriptor, virtualMachine.id());
                } catch (final Exception e) {
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
        log.warn("No virtual machine descriptor, are we on Solaris : ");
        return null;
    }

}