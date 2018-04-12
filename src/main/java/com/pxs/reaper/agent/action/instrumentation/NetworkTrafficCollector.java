package com.pxs.reaper.agent.action.instrumentation;

import com.pxs.reaper.agent.model.NetworkNode;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

@SuppressWarnings("WeakerAccess")
public class NetworkTrafficCollector {

    public static boolean log = Boolean.TRUE;
    public static final NetworkNode NETWORK_NODE = new NetworkNode();

    static {
        try {
            NETWORK_NODE.setLocalAddress(InetAddress.getLocalHost().getHostName());
        } catch (final UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static void collectOutputTraffic(final Socket socket, final int len) {
        InetSocketAddress localAddress = (InetSocketAddress) socket.getLocalSocketAddress();
        String route = localAddress.getHostName() + ":" + localAddress.getPort();
        if (log) {
            System.out.println("Socket output : " + socket + ", length : " + len + ", route : " + route);
        }
        InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        String remoteHostName = remoteAddress.getHostName();
        Integer remotePort = remoteAddress.getPort();

        for (final MutableTriple<String, Integer, Long> mutableTriple : NETWORK_NODE.getAddressPortThroughPut()) {
            if (remoteHostName.equals(mutableTriple.getLeft()) && remotePort.equals(mutableTriple.getMiddle())) {
                mutableTriple.setRight(mutableTriple.getRight() + len);
                return;
            }
        }
        MutableTriple<String, Integer, Long> addressPortThroughPut = new MutableTriple<>(remoteHostName, remotePort, (long) len);
        NETWORK_NODE.getAddressPortThroughPut().add(addressPortThroughPut);
    }

    @SuppressWarnings("unused")
    public static void collectInputTraffic(final Socket socket, final byte[] bytes, final int off, final int len) {
        System.out.println("Socket input : " + socket + ", length : " + len + ", offset : " + off/* + ":" + Arrays.toString(bytes)*/);
    }

}