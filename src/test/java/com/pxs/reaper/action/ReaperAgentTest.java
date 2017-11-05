package com.pxs.reaper.action;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.instrument.Instrumentation;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
@RunWith(MockitoJUnitRunner.class)
public class ReaperAgentTest {

    private String args = null;

    @Spy
    private ReaperAgent reaperAgent;
    @Mock
    private Instrumentation instrumentation;

    @Test
    public void agentmain() throws Exception {
        ReaperAgent.agentmain(args, instrumentation);
        // Thread.sleep(1000 * 60 * 60);
    }

    @Test
    public void premain() throws Exception {
        ReaperAgent.premain(args, instrumentation);
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