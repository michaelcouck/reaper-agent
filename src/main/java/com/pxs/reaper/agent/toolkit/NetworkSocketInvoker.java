package com.pxs.reaper.agent.toolkit;

import com.pxs.reaper.agent.action.instrumentation.NetworkTrafficCollector;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class NetworkSocketInvoker {

    private int port = 8080;

    public static void main(String[] args) throws InterruptedException {
        new NetworkSocketInvoker().writeAndReadFromSocket();
        new NetworkSocketInvoker().writeAndReadFromSocket();
    }

    public void writeAndReadFromSocket() {
        startServerSocket();
        THREAD.sleep(100);
        writeToServerSocket();
    }

    private void startServerSocket() {
        new Thread(() -> {
            while (port++ < Short.MAX_VALUE) {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    try (Socket socket = serverSocket.accept()) {
                        List list = IOUtils.readLines(socket.getInputStream());
                        System.out.println(list);
                        break;
                    } catch (final IOException e) {
                        System.err.println(e.getMessage());
                    }
                } catch (final IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }

    private void writeToServerSocket() {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), port)) {
            // TODO: Capture all calls to this and record the through put
            Class.forName(NetworkTrafficCollector.class.getName());
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("Hello world!".getBytes());
            // TODO: Remove sockets either when explicitly closed or
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

}