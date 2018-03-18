package com.pxs.reaper.action;

import com.pxs.reaper.transport.RestTransport;
import com.pxs.reaper.transport.Transport;
import mockit.Deencapsulation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.instrument.Instrumentation;
import java.util.Properties;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
@RunWith(MockitoJUnitRunner.class)
public class ReaperAgentTest {

    private String args =
            "localhost-jmx-uri=service:jmx:rmi:///jndi/rmi://el5753:1099/jmxrmi|" +
                    "reaper-rest-uri-j-metrics=http://el5753:8090/j-metrics|" +
                    "reaper-rest-uri-o-metrics=http://el5753:8090/o-metrics|" +
                    "reaper-web-socket-uri=ws://el5753:8090/reaper-websocket";

    private Properties properties;
    @Spy
    private ReaperAgent reaperAgent;
    @Mock
    private Instrumentation instrumentation;

    @Before
    public void before() {
        properties = System.getProperties();
    }

    @After
    public void after() {
        System.getProperties().putAll(properties);
    }

    @Test
    public void agentmain() throws Exception {
        ReaperAgent.agentmain(args, instrumentation);
        // Thread.sleep(1000 * 60 * 60);
    }

    @Test
    public void premain() throws Exception {
        ReaperAgent.premain(args, instrumentation);
        Transport transport = new RestTransport();
        String reaperJMetricsRestUri = Deencapsulation.getField(transport, "reaperJMetricsRestUri");
        Assert.assertTrue(reaperJMetricsRestUri.contains("/j-metrics"));
        String reaperOMetricsRestUri = Deencapsulation.getField(transport, "reaperOMetricsRestUri");
        Assert.assertTrue(reaperOMetricsRestUri.contains("/o-metrics"));
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