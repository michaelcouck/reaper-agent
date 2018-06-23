package com.pxs.reaper.agent;

import com.pxs.reaper.agent.toolkit.NetworkSocketInvoker;
import com.pxs.reaper.agent.toolkit.THREAD;
import com.pxs.reaper.agent.transport.RestTransport;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(JMockit.class)
public class ReaperIntegration {

    private static final AtomicInteger TRANSPORT_INVOCATIONS = new AtomicInteger();

    public static final class TransportMock extends MockUp<RestTransport> {

        @Mock
        @SuppressWarnings("unused")
        public boolean postMetrics(final Object metrics) {
            System.out.println("Posting : " + metrics);
            TRANSPORT_INVOCATIONS.incrementAndGet();
            return Boolean.TRUE;
        }

    }

    private Reaper reaper;

    @BeforeClass
    public static void beforeClass() {
        new Thread(() -> {
            NetworkSocketInvoker networkSocketInvoker = new NetworkSocketInvoker();
            //noinspection InfiniteLoopStatement
            while (true) {
                networkSocketInvoker.writeAndReadFromSocket();
                THREAD.sleep(1000);
            }
        }).start();
        new TransportMock();
    }

    @Before
    public void before() {
        reaper = new Reaper();
        TRANSPORT_INVOCATIONS.set(0);
    }

    @Test
    public void main() {
        final AtomicBoolean finished = new AtomicBoolean(Boolean.FALSE);
        new Thread(() -> {
            THREAD.sleep(1000);
            if (!finished.get()) {
                throw new RuntimeException("Didn't finish on time : ");
            }
        }).start();
        Reaper.main(new String[]{"250"});
        finished.set(Boolean.TRUE);
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
    public void attachToOperatingSystem() {
        reaper.attachToOperatingSystem();
        THREAD.sleep(Constant.WAIT_TO_POST_METRICS * 3);
        Assert.assertTrue(TRANSPORT_INVOCATIONS.get() > 0);
    }

    @Test
    public void attachToJavaProcesses() throws IOException, InterruptedException {
        reaper.attachToJavaProcesses();
        THREAD.sleep(Constant.WAIT_TO_POST_METRICS * 3);
        Assert.assertTrue(TRANSPORT_INVOCATIONS.get() > 0);
    }

    @Test
    @Ignore
    @Deprecated
    public void attachToJmxProcesses() {
        // This functionality is deprecated
        reaper.attachToJmxProcesses();
        THREAD.sleep(Constant.WAIT_TO_POST_METRICS * 3);
    }

}