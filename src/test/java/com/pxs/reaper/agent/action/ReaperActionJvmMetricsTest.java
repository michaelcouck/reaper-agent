package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.Reaper;
import com.pxs.reaper.agent.model.JMetrics;
import com.pxs.reaper.agent.toolkit.THREAD;
import com.pxs.reaper.agent.transport.Transport;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@RunWith(MockitoJUnitRunner.class)
public class ReaperActionJvmMetricsTest {

    private final Logger log = Logger.getLogger(this.getClass().getSimpleName());

    @Mock
    private Transport transport;
    private ReaperActionJvmMetrics reaperActionJvmMetrics;

    @Before
    public void before() {
        Reaper.addNativeLibrariesToPath();
        reaperActionJvmMetrics = new ReaperActionJvmMetrics();
        Whitebox.setInternalState(reaperActionJvmMetrics, "transport", transport);
    }

    @Test
    public void run() throws IOException {
        AtomicReference<JMetrics> objectAtomicReference = new AtomicReference<>();
        //noinspection Duplicates
        Mockito.doAnswer(invocation -> {
            Object[] metrics = invocation.getArguments();
            for (final Object metric : metrics) {
                if (metric != null && JMetrics.class.isAssignableFrom(metric.getClass())) {
                    objectAtomicReference.set((JMetrics) metric);
                    JMetrics jMetrics = (JMetrics) metric;
                    log.info("Metric : " + jMetrics.getCodeBase());
                }
            }
            return null;
        }).when(transport).postMetrics(Mockito.any(Object.class));
        reaperActionJvmMetrics.run();
        Assert.assertNotNull(objectAtomicReference.get());
        Assert.assertNotNull(objectAtomicReference.get().getIpAddress());
        Assert.assertTrue(objectAtomicReference.get().getUserDir().contains(JMetrics.class.getSimpleName()));
    }

    @Test
    public void terminate() throws IOException {
        // Constant.TIMER.scheduleAtFixedRate(reaperActionJvmMetrics, Short.MAX_VALUE, Short.MAX_VALUE);
        THREAD.scheduleAtFixedRate(reaperActionJvmMetrics, Short.MAX_VALUE, Short.MAX_VALUE);
        // The result of the task is dependant on the state and if it has been executed already, so we just run the code
        reaperActionJvmMetrics.terminate();
    }

    @Test
    public void applicationName() {
        log.info(FilenameUtils.getName(System.getProperty("user.dir")));
    }

    @Test
    public void codeBase() {
        JMetrics jMetrics = new JMetrics();
        log.info("n) : " + jMetrics.getCodeBase());
        log.info("1) : " + jMetrics.getClass().getProtectionDomain().getCodeSource().getLocation());
        log.info("2) : " + jMetrics.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        log.info("3) : " + jMetrics.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        log.info("4) : " + new File(jMetrics.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName());
    }

}
