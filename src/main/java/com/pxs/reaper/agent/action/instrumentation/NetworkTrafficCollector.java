package com.pxs.reaper.agent.action.instrumentation;

import com.pxs.reaper.agent.model.NetworkNode;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class NetworkTrafficCollector {

    public static boolean log = Boolean.TRUE;
    public static final Map<String, NetworkNode> NETWORK_ROUTES = new HashMap<>();

    public static void collectOutputTraffic(final Socket socket, final int len) {
        InetSocketAddress localAddress = (InetSocketAddress) socket.getLocalSocketAddress();
        String route = localAddress.getHostName() + ":" + localAddress.getPort();
        if (log) {
            System.out.println("Socket output : " + socket + ", length : " + len + ", route : " + route);
        }

        NetworkNode networkNode = NETWORK_ROUTES.get(route);
        if (networkNode == null) {
            networkNode = new NetworkNode();
            networkNode.setLocalPort(localAddress.getPort());
            networkNode.setLocalAddress(localAddress.getHostName());
            NETWORK_ROUTES.put(route, networkNode);
        }

        InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        networkNode.getRemotePorts().add(remoteAddress.getPort());
        networkNode.getRemoteAddresses().add(remoteAddress.getHostName());

        networkNode.setOutput(networkNode.getOutput() + len);
    }

    @SuppressWarnings("unused")
    public static void collectInputTraffic(final Socket socket, final byte[] bytes, final int off, final int len) {
        System.out.println("Socket input : " + socket + ", length : " + len + ", offset : " + off/* + ":" + Arrays.toString(bytes)*/);
    }

}