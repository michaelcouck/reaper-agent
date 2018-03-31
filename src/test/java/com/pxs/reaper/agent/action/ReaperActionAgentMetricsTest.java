package com.pxs.reaper.agent.action;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionAgentMetricsTest {

    @Spy
    private ReaperActionAgentMetrics reaperActionAgentMetrics;

    @Test
    @Ignore
    public void attachToJavaProcesses() {
        reaperActionAgentMetrics.attachToJavaProcesses();
    }

}
