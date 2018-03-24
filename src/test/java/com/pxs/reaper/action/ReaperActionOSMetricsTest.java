package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.Reaper;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.toolkit.THREAD;
import com.pxs.reaper.transport.Transport;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionOSMetricsTest {

    @Mock
    private Transport transport;
    private ReaperActionOSMetrics reaperActionOSMetrics;

    @BeforeClass
    public static void beforeClass() {
        Reaper.addNativeLibrariesToPath();
    }

    @Before
    public void before() {
        Reaper.addNativeLibrariesToPath();
        reaperActionOSMetrics = new ReaperActionOSMetrics();
        Deencapsulation.setField(Constant.class, "TRANSPORT", transport);
    }

    @Test
    public void run() {
        AtomicReference<Object> objectAtomicReference = new AtomicReference<>();
        Mockito.doAnswer(invocation -> {
            Object[] metrics = invocation.getArguments();
            for (final Object metric : metrics) {
                if (metric != null && OSMetrics.class.isAssignableFrom(metric.getClass())) {
                    System.out.println(Constant.GSON.toJson(metric));
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
        THREAD.scheduleAtFixedRate(reaperActionOSMetrics, Short.MAX_VALUE, Short.MAX_VALUE);
        // The result of the task is dependant on the state and if it has been executed already, so we just run the code
        reaperActionOSMetrics.terminate();
    }

}