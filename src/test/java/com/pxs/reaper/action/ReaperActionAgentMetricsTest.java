package com.pxs.reaper.action;

import com.pxs.reaper.toolkit.THREAD;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class ReaperActionAgentMetricsTest {

    @Spy
    private ReaperActionAgentMetrics reaperActionAgentMetrics;

    @Test
    public void run() {
        reaperActionAgentMetrics.run();
        THREAD.sleep(30000);
    }

}
