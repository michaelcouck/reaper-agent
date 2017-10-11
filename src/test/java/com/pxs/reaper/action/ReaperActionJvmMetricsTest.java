package com.pxs.reaper.action;

import com.pxs.reaper.Transport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionJvmMetricsTest {

    @Mock
    private Transport transport;
    @Spy
    private ReaperActionJvmMetrics reaperActionJvmMetrics;

    @Test
    public void ReaperActionJvmMetrics() throws IOException {
        // TODO: All the properties are set
        // TODO: Assert the task is registered in the timer
    }

    @Test
    public void run() throws IOException {
        reaperActionJvmMetrics.run();
    }

    @Test
    public void terminate() throws IOException {
        reaperActionJvmMetrics.terminate();
        // TODO: Verify that the task will not run again
    }

}
