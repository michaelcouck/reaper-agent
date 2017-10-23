package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.Reaper;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.transport.Transport;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ReaperActionOSMetricsTest {

    @Mock
    private Transport transport;
    private ReaperActionOSMetrics reaperActionOSMetrics;

    @Before
    public void before() {
        Reaper.addNativeLibrariesToPath();
        reaperActionOSMetrics = new ReaperActionOSMetrics();
        Whitebox.setInternalState(reaperActionOSMetrics, "transport", transport);
    }

    @Test
    public void run() {
        AtomicReference<Object> objectAtomicReference = new AtomicReference<>();
        Mockito.doAnswer(invocation -> {
            Object[] metrics = invocation.getArguments();
            for (final Object metric : metrics) {
                if (metric != null && OSMetrics.class.isAssignableFrom(metric.getClass())) {
                    objectAtomicReference.set(metric);
                }
            }
            return null;
        }).when(transport).postMetrics(Mockito.any(Object.class));
        reaperActionOSMetrics.run();
        Assert.assertNotNull(objectAtomicReference.get());
    }

    @Test
    public void terminate() {
        Constant.TIMER.scheduleAtFixedRate(reaperActionOSMetrics, Short.MAX_VALUE, Short.MAX_VALUE);
        boolean terminated = reaperActionOSMetrics.terminate();
        Assert.assertTrue(terminated);
    }

    @Test
    public void getHostname() throws Exception {
        String hostName = ReaperAction.Hostname.hostname();
        Assert.assertNotNull(hostName);
    }

}