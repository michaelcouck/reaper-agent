package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.transport.Transport;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ReaperActionJvmMetricsTest {

    @Mock
    private Transport transport;
    @Spy
    @InjectMocks
    private ReaperActionJvmMetrics reaperActionJvmMetrics;

    @Test
    public void run() throws IOException {
        AtomicReference<Object> objectAtomicReference = new AtomicReference<>();
        Mockito.doAnswer(invocation -> {
            Object[] metrics = invocation.getArguments();
            for (final Object metric : metrics) {
                if (metric != null && JMetrics.class.isAssignableFrom(metric.getClass())) {
                    objectAtomicReference.set(metric);
                }
            }
            return null;
        }).when(transport).postMetrics(Mockito.any(Object.class));
        reaperActionJvmMetrics.run();
        Assert.assertNotNull(objectAtomicReference.get());
    }

    @Test
    public void terminate() throws IOException {
        Constant.TIMER.scheduleAtFixedRate(reaperActionJvmMetrics, Short.MAX_VALUE, Short.MAX_VALUE);
        boolean terminated = reaperActionJvmMetrics.terminate();
        Assert.assertTrue(terminated);
    }

}
