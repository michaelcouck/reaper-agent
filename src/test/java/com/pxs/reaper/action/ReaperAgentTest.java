package com.pxs.reaper.action;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.instrument.Instrumentation;

/**
 * TODO: Complete this tests.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-10-2017
 */
@RunWith(MockitoJUnitRunner.class)
public class ReaperAgentTest {

    @Spy
    private ReaperAgent reaperAgent;

    private String args = "";
    @Mock
    private Instrumentation instrumentation;

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void agentmain() throws Exception {
        ReaperAgent.agentmain(args, instrumentation);
    }

    @Test
    public void premain() throws Exception {
        ReaperAgent.premain(args, instrumentation);
    }

    @Test
    public void transform() throws Exception {
        reaperAgent.transform(
                ClassLoader.getSystemClassLoader(),
                ReaperAgent.class.getName(),
                ReaperAgent.class,
                ReaperAgent.class.getProtectionDomain(),
                new byte[]{});
    }

}