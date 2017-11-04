package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.Reaper;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.transport.Transport;
import lombok.extern.slf4j.Slf4j;
import org.hyperic.sigar.NetConnection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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

    @BeforeClass
    public static void beforeClass() {
        Reaper.addNativeLibrariesToPath();
    }

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
                    System.out.println(Constant.GSON.toJson(metric));
                    for (NetConnection netConnection : ((OSMetrics) metric).getNetConnections()) {
                        System.out.println(netConnection.getRemoteAddress());
                    }
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
        // The result of the task is dependant on the state and if it has been executed already, so we just run the code
        reaperActionOSMetrics.terminate();
    }

}