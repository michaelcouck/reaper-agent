package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.Reaper;
import com.pxs.reaper.model.JMetrics;
import com.pxs.reaper.transport.Transport;
import lombok.extern.slf4j.Slf4j;
import mockit.Deencapsulation;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class ReaperActionJvmMetricsTest {

    @Mock
    private Transport transport;
    private ReaperActionJvmMetrics reaperActionJvmMetrics;

    @Before
    public void before() {
        Reaper.addNativeLibrariesToPath();
        reaperActionJvmMetrics = new ReaperActionJvmMetrics();
        Deencapsulation.setField(Constant.class, "TRANSPORT", transport);
    }

    @Test
    public void run() throws IOException {
        AtomicReference<Object> objectAtomicReference = new AtomicReference<>();
        //noinspection Duplicates
        Mockito.doAnswer(invocation -> {
            Object[] metrics = invocation.getArguments();
            for (final Object metric : metrics) {
                if (metric != null && JMetrics.class.isAssignableFrom(metric.getClass())) {
                    objectAtomicReference.set(metric);
                    JMetrics jMetrics = (JMetrics) metric;
                    log.info("Metric : " + jMetrics.getCodeBase());
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
        // The result of the task is dependant on the state and if it has been executed already, so we just run the code
        reaperActionJvmMetrics.terminate();
    }

    @Test
    public void applicationName() {
        log.info(FilenameUtils.getName(System.getProperty("user.dir")));
    }

    @Test
    @Ignore
    public void codeBase() {
        JMetrics jMetrics = new JMetrics();
        log.info("n) : " + jMetrics.getCodeBase());
        log.info("1) : " + jMetrics.getClass().getProtectionDomain().getCodeSource().getLocation());
        log.info("2) : " + jMetrics.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        log.info("3) : " + jMetrics.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
        log.info("4) : " + new File(jMetrics.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName());
    }

}
