package com.pxs.reaper.action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionAgentMetricsTest {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    @Spy
    private ReaperActionAgentMetrics reaperActionAgentMetrics;

    @Test
    public void attachToJavaProcesses() {
        reaperActionAgentMetrics.attachToJavaProcesses();
    }

}
