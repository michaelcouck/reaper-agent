package com.pxs.reaper.agent.action.instrumentation;

import org.junit.Before;
import org.junit.Test;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NetworkTrafficCollectorTest {

    private Socket socket;

    @Before
    public void before() throws IOException, RunnerException {
        socket = new Socket("ikube.be", 80);
    }

    @Test
    public void collectOutputTraffic() throws IOException {
        NetworkTrafficCollector.log = Boolean.FALSE;
        double iterations = 1000000;
        double start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            NetworkTrafficCollector.collectOutputTraffic(socket, 1024);
        }
        double end = System.currentTimeMillis();
        double duration = end - start;
        double iterationsPerSecond = iterations / duration;
        System.out.println("Iterations per second : " + iterationsPerSecond);
        NetworkTrafficCollector.log = Boolean.TRUE;
    }

}