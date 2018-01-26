package com.pxs.reaper;

import mockit.Deencapsulation;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(MockitoJUnitRunner.class)
public class ReaperIntegration {

    @Mock
    private Timer timer;
    @Spy
    private Reaper reaper;

    @Before
    public void before() {
        Constant.PROPERTIES_INJECTOR.injectProperties(Constant.TRANSPORT);
        Constant.PROPERTIES_INJECTOR.injectProperties(Constant.TRANSPORT_REST);
        Constant.PROPERTIES_INJECTOR.injectProperties(Constant.TRANSPORT_WEB_SOCKET);
    }

    @Test
    public void main() {
        Deencapsulation.setField(Constant.class, "TIMER", timer);
        AtomicInteger scheduledCount = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            scheduledCount.incrementAndGet();
            return null;
        }).when(timer).scheduleAtFixedRate(Mockito.any(TimerTask.class), Mockito.anyLong(), Mockito.anyLong());
        Reaper.main(new String[]{"250"});
        Assert.assertEquals(3, scheduledCount.get());
    }

    @Test
    public void addNativeLibrariesToPath() throws IOException {
        String javaLibraryPath = Reaper.addNativeLibrariesToPath();
        String[] paths = StringUtils.split(javaLibraryPath, File.pathSeparatorChar);
        for (final String path : paths) {
            if (Arrays.deepToString(new File(path).list()).contains(Constant.LINUX_LOAD_MODULE)) {
                return;
            }
        }
        Assert.fail("Should contain the link libraries");
    }

    @Test
    public void attachToOperatingSystem() throws InterruptedException {
        reaper.attachToOperatingSystem();
        Thread.sleep(20000);
    }

    @Test
    public void attachToJavaProcesses() throws InterruptedException {
        reaper.attachToJavaProcesses();
        Thread.sleep(20000);
    }

    @Test
    public void attachToJmxProcesses() {
        reaper.attachToJmxProcesses();
    }

}