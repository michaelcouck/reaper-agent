package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.transport.Transport;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionJmxMetricsTest {

    @Mock
    private Transport transport;
    @Spy
    @InjectMocks
    private ReaperActionJmxMetrics reaperActionJmxMetrics;

    @Before
    public void before() {
        Deencapsulation.setField(Constant.class, "TRANSPORT", transport);
    }

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
        reaperActionJmxMetrics.run();
        Assert.assertNotNull(objectAtomicReference.get());
    }

    @Test
    public void terminate() throws IOException {
        Constant.TIMER.scheduleAtFixedRate(reaperActionJmxMetrics, Short.MAX_VALUE, Short.MAX_VALUE);
        // The result of the task is dependant on the state and if it has been executed already, so we just run the code
        reaperActionJmxMetrics.terminate();
    }

}
