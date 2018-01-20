package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.toolkit.HOST;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ReaperActionAgentMetricsIntegration {

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

            Map<String, VirtualMachine> virtualMachines = Deencapsulation.getField(reaperActionAgentMetrics, "virtualMachines");
            int virtualMachinesSize = virtualMachines.size();
            Assert.assertTrue(virtualMachinesSize > 0);

            reaperActionAgentMetrics.attachToJavaProcesses();
            virtualMachines = Deencapsulation.getField(reaperActionAgentMetrics, "virtualMachines");
            Assert.assertEquals(virtualMachinesSize, virtualMachines.size());
        } finally {
            reaperActionAgentMetrics.detachFromJavaProcesses();
        }
    }

    @Test
    // @Ignore
    public void attachToJmxProcess() {
        ReaperActionJmxMetrics reaperActionJmxMetrics = new ReaperActionJmxMetrics();
        Constant.PROPERTIES_INJECTOR.injectProperties(reaperActionJmxMetrics);
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
