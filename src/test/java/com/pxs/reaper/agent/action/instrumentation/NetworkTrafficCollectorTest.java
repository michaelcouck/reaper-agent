package com.pxs.reaper.agent.action.instrumentation;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkTrafficCollectorTest {

    private Socket socket;
    private ServerSocket serverSocket;

    @Before
    public void before() throws IOException {
        serverSocket = new ServerSocket(8101);
        socket = new Socket("localhost", 8101);
    }

    @After
    public void after() throws IOException {
        socket.close();
        serverSocket.close();
    }

    @Test
    public void collectTraffic() throws IOException {
        NetworkTrafficCollector.log = Boolean.FALSE;
        double iterations = 1000000;
        double start = System.currentTimeMillis();
        System.out.println(socket);
        for (int i = 0; i < iterations; i++) {
            NetworkTrafficCollector.collectTraffic(socket, 1024);
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