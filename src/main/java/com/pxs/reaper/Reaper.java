package com.pxs.reaper;

import com.jcabi.manifests.Manifests;
import com.pxs.reaper.action.ReaperActionOSMetrics;
import com.sun.tools.attach.*;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import lombok.extern.slf4j.Slf4j;
import sun.jvmstat.monitor.MonitoredHost;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class Reaper {

    public static void main(final String[] args) throws Exception {
        addToolsToClassPath();
        Properties properties = System.getProperties();
        for (final Map.Entry<Object, Object> mapEntry : properties.entrySet()) {
            log.warn(mapEntry.getKey() + ":" + mapEntry.getValue());
        }
        new Reaper().reap();
    }

    private static void addToolsToClassPath() throws Exception {
        // Load the tools.jar
        String javaHome = System.getProperty("java.home");
        int upDirectories = javaHome.contains("jre") ? 2 : 0;
        File toolsJar = FILE.findFileRecursively(new File(javaHome), upDirectories, "tools.jar");

        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{toolsJar.toURI().toURL()}, System.class.getClassLoader());
        JarFile jarFile = new JarFile(toolsJar);
        Enumeration<JarEntry> e = jarFile.entries();
        while (e.hasMoreElements()) {
            JarEntry je = e.nextElement();
            if (je.isDirectory() || !je.getName().endsWith(".class")) {
                continue;
            }
            // -6 because of .class
            String className = je.getName().substring(0, je.getName().length() - 6);
            className = className.replace('/', '.');
            log.info("Loading class : " + className);
            urlClassLoader.loadClass(className);
        }
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

    void reap() throws Exception {
        // Start the action to gather metrics from the operating system
        new ReaperActionOSMetrics();
        // Attach the agent to any Java processes that are running on the local machine
        attachToJavaProcesses();
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
    private void attachToJavaProcesses() throws Exception {
        String vmName = ManagementFactory.getRuntimeMXBean().getName();
        log.warn("VM Name : " + vmName);
        VirtualMachineDescriptor ourOwnDescriptor = getMachineDescriptor(vmName);

        String jarFileName = Manifests.read("Agent-Jar-Name");
        File agentJar = FILE.findFileRecursively(new File("."), jarFileName);
        if (agentJar == null) {
            log.warn("Agent jar not found : ");
        } else {
            String pathToAgentJar = FILE.cleanFilePath(agentJar.getAbsolutePath());
            // String pathToAgentJar = ClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            log.warn("Virtual machines : " + VirtualMachine.list());
            MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost("localhost");
            for (final Integer pid : monitoredHost.activeVms()) {
                VirtualMachine virtualMachine;
                try {
                    // virtualMachine = new LinuxVirtualMachine(ATTACH_PROVIDER, pid);
                    virtualMachine = VirtualMachine.attach(String.valueOf(pid));
                    virtualMachine.loadAgent(pathToAgentJar);
                    virtualMachines.add(virtualMachine);
                    log.error("Attached to running java process : " + pid);
                } catch (final AttachNotSupportedException | IOException | AgentInitializationException | AgentLoadException e) {
                    log.error("Exception attaching to java process : " + pid, e);
                }
            }

            /*for (final VirtualMachineDescriptor virtualMachineDescriptor : VirtualMachine.list()) {
                if (virtualMachineDescriptor.equals(ourOwnDescriptor)) {
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
            }*/
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