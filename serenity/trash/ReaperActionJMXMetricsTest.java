package com.pxs.reaper.action;

import com.pxs.reaper.model.Metrics;
import org.junit.Test;

import java.io.IOException;

public class ReaperActionJMXMetricsTest {

    @Test
    public void ReaperActionJMXMetrics() throws IOException {
        ReaperActionJMXMetrics reaperActionJMXMetrics = new ReaperActionJMXMetrics();
        reaperActionJMXMetrics.setMetrics(Metrics.builder().build());
        reaperActionJMXMetrics.run();
    }

}
