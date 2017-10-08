package com.pxs.reaper.action;

import com.pxs.reaper.model.Metrics;
import org.junit.Test;

import java.io.IOException;

public class ReaperActionJvmMetricsTest {

    @Test
    public void ReaperActionJMXMetrics() throws IOException {
        ReaperActionJvmMetrics reaperActionJvmMetrics = new ReaperActionJvmMetrics();
        reaperActionJvmMetrics.setMetrics(Metrics.builder().build());
        reaperActionJvmMetrics.run();
    }

}
