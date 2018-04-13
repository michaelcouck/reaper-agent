package com.pxs.reaper.agent.action.instrumentation;

import com.pxs.reaper.agent.model.NetworkNode;
import com.pxs.reaper.agent.toolkit.HOST;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.WeakHashMap;

@SuppressWarnings("WeakerAccess")
public class NetworkTrafficCollector {

    public static boolean log = Boolean.TRUE;

    public static final NetworkNode NETWORK_NODE = new NetworkNode();

    public static final WeakHashMap<Integer, Integer> SOCKET_PORT = new WeakHashMap<>();
    public static final WeakHashMap<Integer, String> SOCKET_ADDRESS = new WeakHashMap<>();

    static {
        try {
            NETWORK_NODE.setLocalAddress(HOST.hostname());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void collectOutputTraffic(final Socket socket, final int len) {
        if (log) {
            System.out.println("Reaper : Socket output - " + socket + ", length : " + len);
        }

        Integer remotePort;
        String remoteHostName;

        int socketHash = socket.hashCode();

        if (!SOCKET_ADDRESS.containsKey(socketHash)) {
            InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
            SOCKET_PORT.put(socketHash, remoteAddress.getPort());
            SOCKET_ADDRESS.put(socketHash, remoteAddress.getAddress().getHostAddress());
        }
        remotePort = SOCKET_PORT.get(socketHash);
        remoteHostName = SOCKET_ADDRESS.get(socketHash);

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
        System.out.println("Reaper : Socket input - " + socket + ", length : " + len + ", offset : " + off/* + ":" + Arrays.toString(bytes)*/);
    }

}