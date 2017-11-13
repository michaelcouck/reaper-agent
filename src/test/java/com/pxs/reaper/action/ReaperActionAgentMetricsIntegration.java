package com.pxs.reaper.action;

import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

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

}
