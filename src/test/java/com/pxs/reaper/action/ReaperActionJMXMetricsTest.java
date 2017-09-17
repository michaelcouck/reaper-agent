package com.pxs.reaper.action;

import com.pxs.reaper.Reaper;
import org.junit.Test;

import java.io.IOException;

public class ReaperActionJMXMetricsTest {

    @Test
    public void ReaperActionJMXMetrics() throws IOException {
        Reaper reaper = new Reaper();
        ReaperActionJMXMetrics reaperActionJMXMetrics = new ReaperActionJMXMetrics(reaper.getReaperWebSocketUri());
        reaperActionJMXMetrics.run();
    }

}
