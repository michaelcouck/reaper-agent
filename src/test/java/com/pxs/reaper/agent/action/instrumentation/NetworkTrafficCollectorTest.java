package com.pxs.reaper.agent.action.instrumentation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
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
        socket = new Socket("192.168.1.21", 8080);
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
        double iterationsPerSecond = iterations / duration * 1000;
        System.out.println("Iterations : " + iterations + ", duration : " + duration + "Iterations per second : " + iterationsPerSecond);
        NetworkTrafficCollector.log = Boolean.TRUE;
    }

    @Test
    public void network() throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet getMethod = new HttpGet("https://google.com");
        HttpResponse response = httpClient.execute(getMethod);
        System.out.println("HTTP Status of response: " + response.getStatusLine().getStatusCode());
    }

}