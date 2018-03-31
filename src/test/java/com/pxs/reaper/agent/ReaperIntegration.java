package com.pxs.reaper.agent;

import com.pxs.reaper.agent.toolkit.THREAD;
import com.pxs.reaper.agent.transport.Transport;
import mockit.Deencapsulation;
import org.apache.commons.lang.StringUtils;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.api.PropertiesInjector;
import org.junit.After;
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

@RunWith(MockitoJUnitRunner.class)
public class ReaperIntegration {

    @Spy
    private Reaper reaper;
    @Mock
    private Transport transport;

    @Before
    public void before() {
        Deencapsulation.setField(Constant.class, "SLEEP_TIME", 1000);
    }

    @After
    public void after() {
        PropertiesInjector propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
        propertiesInjector.injectProperties(Constant.class);
    }

    @Test
    public void main() {
        Reaper.main(new String[]{"250"});
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
        THREAD.sleep(3000);
        Mockito.verify(transport, Mockito.atLeast(1)).postMetrics(Mockito.anyObject());
    }

    @Test
    public void attachToJavaProcesses() {
        reaper.attachToJavaProcesses();
        THREAD.sleep(30000);
        Mockito.verify(transport, Mockito.atLeast(1)).postMetrics(Mockito.anyObject());
    }

    @Test
    public void attachToJmxProcesses() {
        reaper.attachToJmxProcesses();
        THREAD.sleep(3000);
        Mockito.verify(transport, Mockito.atLeast(1)).postMetrics(Mockito.anyObject());
    }

}