package com.pxs.reaper.action;

import com.pxs.reaper.Reaper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionOSMetricsTest {

    private ReaperActionOSMetrics reaperActionOSMetrics;

    @Before
    public void before() {
        Reaper.addNativeLibrariesToPath();
        reaperActionOSMetrics = new ReaperActionOSMetrics();
    }

    @Test
    public void run() {
        // TODO: Test the result
        reaperActionOSMetrics.run();
    }

}
