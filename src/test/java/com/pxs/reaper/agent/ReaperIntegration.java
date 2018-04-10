package com.pxs.reaper.agent;

import com.pxs.reaper.agent.toolkit.NetworkSocketInvoker;
import com.pxs.reaper.agent.toolkit.THREAD;
import com.pxs.reaper.agent.transport.Transport;
import org.apache.commons.lang.StringUtils;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.api.PropertiesInjector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class ReaperIntegration {

    @Spy
    private Reaper reaper;
    @Mock
    private Transport transport;

    @After
    public void after() {
        PropertiesInjector propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
        propertiesInjector.injectProperties(Constant.class);
    }

    @Test
    @Ignore
    public void main() {
        Reaper.main(new String[]{"250"});
    }

    @Test
    @Ignore
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
    @Ignore
    public void attachToOperatingSystem() {
        reaper.attachToOperatingSystem();
        THREAD.sleep(Constant.SLEEP_TIME * 3);
        // Mockito.verify(transport, Mockito.atLeast(1)).postMetrics(Mockito.anyObject());
    }

    @Test
    public void attachToJavaProcesses() throws IOException, InterruptedException {
        reaper.attachToJavaProcesses();
        THREAD.sleep(Constant.SLEEP_TIME * 4);
        new NetworkSocketInvoker().writeAndReadFromSocket();
        THREAD.sleep(Constant.SLEEP_TIME);
        // Mockito.verify(transport, Mockito.atLeast(1)).postMetrics(Mockito.anyObject());
    }

    @Test
    public void attachToJmxProcesses() {
        reaper.attachToJmxProcesses();
        THREAD.sleep(Constant.SLEEP_TIME * 3);
        // Mockito.verify(transport, Mockito.atLeast(1)).postMetrics(Mockito.anyObject());
    }

}