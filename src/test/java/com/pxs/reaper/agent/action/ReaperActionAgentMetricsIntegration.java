package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.toolkit.HOST;
import com.sun.tools.attach.VirtualMachine;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.api.PropertiesInjector;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionAgentMetricsIntegration {

    private final Logger log = Logger.getLogger(this.getClass().getSimpleName());

    @Mock
    private VirtualMachine virtualMachine;

    @Test
    public void detachFromJavaProcesses() {
        final String id = "virtual-machine";
        ReaperActionAgentMetrics reaperActionAgentMetrics = new ReaperActionAgentMetrics();
        Map<String, VirtualMachine> virtualMachines = new HashMap<>();
        virtualMachines.put(id, virtualMachine);
        Whitebox.setInternalState(reaperActionAgentMetrics, "virtualMachines", virtualMachines);
        reaperActionAgentMetrics.detachFromJavaProcesses();
        Assert.assertEquals(0, virtualMachines.size());
    }

    @Test
    public void attachToJavaProcesses() throws Exception {
        ReaperActionAgentMetrics reaperActionAgentMetrics = new ReaperActionAgentMetrics();
        try {
            reaperActionAgentMetrics.attachToJavaProcesses();

            Map<String, VirtualMachine> virtualMachines = getFieldValue(reaperActionAgentMetrics, "virtualMachines");
            int virtualMachinesSize = virtualMachines.size();
            Assert.assertTrue(virtualMachinesSize > 0);

            reaperActionAgentMetrics.attachToJavaProcesses();
            virtualMachines = getFieldValue(reaperActionAgentMetrics, "virtualMachines");
            Assert.assertEquals(virtualMachinesSize, virtualMachines.size());
        } finally {
            reaperActionAgentMetrics.detachFromJavaProcesses();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(final Object object, final String fieldName) throws IllegalAccessException {
        for (final Field field : object.getClass().getFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(Boolean.TRUE);
                return (T) field.get(object);
            }
        }
        throw new RuntimeException("Field does not exist : ");
    }

    @Test
    public void attachToJmxProcess() {
        // TODO: This needs to be fleshed out, not needed at the moment...
        ReaperActionJmxMetrics reaperActionJmxMetrics = new ReaperActionJmxMetrics();
        PropertiesInjector propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
        propertiesInjector.injectProperties(reaperActionJmxMetrics);
        reaperActionJmxMetrics.run();
    }

    @Test
    public void monitoredHosts() throws MonitorException, URISyntaxException, SocketException {
        Collection<String> ipAddresses = HOST.ipAddressesForLocalHost(NetworkInterface.getNetworkInterfaces());
        for (final String ipAddress : ipAddresses) {
            log.info("Ip address : " + ipAddress);
            try {
                MonitoredHost monitoredHost = MonitoredHost.getMonitoredHost(ipAddress);
                log.info("Monitored host : " + monitoredHost);
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

}
