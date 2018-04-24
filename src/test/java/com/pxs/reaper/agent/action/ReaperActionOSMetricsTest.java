package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.Reaper;
import com.pxs.reaper.agent.model.OSMetrics;
import com.pxs.reaper.agent.toolkit.THREAD;
import com.pxs.reaper.agent.transport.Transport;
import org.hyperic.sigar.*;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionOSMetricsTest {

    private Logger logger = Logger.getLogger(ReaperActionOSMetricsTest.class.getName());

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
                    objectAtomicReference.set(metric);
                }
            }
            return null;
        }).when(transport).postMetrics(Mockito.any(Object.class));
        reaperActionOSMetrics.run();
        Assert.assertNotNull(objectAtomicReference.get());
    }

    @Test
    public void sigar() throws SigarException {
        Sigar sigar = new Sigar();
        SigarProxy sigarProxy = SigarProxyCache.newInstance(sigar, 1000);
        String[] interfaces = sigarProxy.getNetInterfaceList();
        int iterations = 3;
        do {
            for (final String interfase : interfaces) {
                if (interfase.contains("wlp")) {
                    NetInterfaceStat netInterfaceStat = sigarProxy.getNetInterfaceStat(interfase);
                    logger.log(Level.SEVERE, "Speed : " + netInterfaceStat.getSpeed());
                    logger.log(Level.SEVERE, "Rx bytes : " + netInterfaceStat.getRxBytes());
                    logger.log(Level.SEVERE, "Tx collisions : " + netInterfaceStat.getTxCollisions());
                }
            }
            THREAD.sleep(1000);
        } while (iterations-- > 0);
    }

    @Test
    public void terminate() {
        THREAD.scheduleAtFixedRate(reaperActionOSMetrics, Short.MAX_VALUE, Short.MAX_VALUE);
        // The result of the task is dependant on the state and if it has been executed already, so we just run the code
        reaperActionOSMetrics.terminate();
    }

}