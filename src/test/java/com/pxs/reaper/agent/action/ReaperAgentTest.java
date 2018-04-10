package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.toolkit.MANIFEST;
import lombok.Setter;
import org.jeasy.props.PropertiesInjectorBuilder;
import org.jeasy.props.annotations.Property;
import org.jeasy.props.api.PropertiesInjector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
@Setter
@RunWith(MockitoJUnitRunner.class)
public class ReaperAgentTest {

    private String args =
            "localhost-jmx-uri=service:jmx:rmi:///jndi/rmi://el5753:1099/jmxrmi|" +
                    "reaper-rest-uri-j-metrics=http://localhost:8090/j-metrics|" +
                    "reaper-rest-uri-o-metrics=http://localhost:8090/o-metrics|" +
                    "reaper-web-socket-uri=ws://localhost:8090/reaper-websocket";

    private Properties properties;

    @SuppressWarnings("unused")
    @Property(source = Constant.REAPER_PROPERTIES, key = "sleep-time")
    private int sleepTime;
    @Spy
    private ReaperAgent reaperAgent;
    @Mock
    private Instrumentation instrumentation;

    @Before
    public void before() {
        properties = System.getProperties();
        PropertiesInjector propertiesInjector = PropertiesInjectorBuilder.aNewPropertiesInjector();
        propertiesInjector.injectProperties(this);
        // PROPERTIES_INJECTOR.injectProperties(Constant.EXTERNAL_CONSTANTS);
    }

    @After
    public void after() {
        System.getProperties().putAll(properties);
    }

    @Test
    public void agentmain() throws Exception {
        ReaperAgent.agentmain(args, instrumentation);
        // Thread.sleep((long) (sleepTime * 1.5));
    }

    @Test
    public void premain() throws Exception {
        ReaperAgent.premain(args, instrumentation);
        // Thread.sleep((long) (sleepTime * 30));
    }

    @Test
    public void getClassPathUrls() {
        URL[] urls = MANIFEST.getClassPathUrls();
        Assert.assertNotNull(urls);
        Assert.assertTrue(Arrays.toString(urls).contains("reaper"));
    }

    @Test
    public void transform() throws Exception {
        // There is no instrumentation at the time of writing this test
        reaperAgent.transform(
                ClassLoader.getSystemClassLoader(),
                ReaperAgent.class.getName(),
                ReaperAgent.class,
                ReaperAgent.class.getProtectionDomain(),
                new byte[]{});
    }

}