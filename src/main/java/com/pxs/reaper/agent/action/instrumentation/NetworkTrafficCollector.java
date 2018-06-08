package com.pxs.reaper.agent.action.instrumentation;

import com.pxs.reaper.agent.model.NetworkNode;
import com.pxs.reaper.agent.model.Quadruple;
import com.pxs.reaper.agent.toolkit.HOST;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.WeakHashMap;

@SuppressWarnings("WeakerAccess")
public class NetworkTrafficCollector {

    public static boolean log = Boolean.FALSE;

    public static final NetworkNode NETWORK_NODE = new NetworkNode();

    public static final WeakHashMap<Integer, Integer> LOCAL_SOCKET_PORT = new WeakHashMap<>();
    public static final WeakHashMap<Integer, Integer> REMOTE_SOCKET_PORT = new WeakHashMap<>();
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

        Integer localPort;
        Integer remotePort;
        String remoteAddress;

        int socketHash = socket.hashCode();

        if (!SOCKET_ADDRESS.containsKey(socketHash)) {
            InetSocketAddress localInetSocketAddress = (InetSocketAddress) socket.getLocalSocketAddress();
            InetSocketAddress remoteInetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

            localPort = localInetSocketAddress.getPort();
            remotePort = remoteInetSocketAddress.getPort();
            remoteAddress = remoteInetSocketAddress.getAddress().getHostAddress();

            collectOutputTraffic(socketHash, localPort, remotePort, remoteAddress, len);
        }

        localPort = LOCAL_SOCKET_PORT.get(socketHash);
        remotePort = REMOTE_SOCKET_PORT.get(socketHash);
        remoteAddress = SOCKET_ADDRESS.get(socketHash);

        collectOutputTraffic(socketHash, localPort, remotePort, remoteAddress, len);
    }

    public static void collectOutputTraffic(final int socketHash, final int localPort, final int remotePort, final String remoteAddress, final long len) {
        if (!SOCKET_ADDRESS.containsKey(socketHash)) {
            LOCAL_SOCKET_PORT.put(socketHash, localPort);
            REMOTE_SOCKET_PORT.put(socketHash, remotePort);
            SOCKET_ADDRESS.put(socketHash, remoteAddress);
        }

        for (final Quadruple<Integer, String, Integer, Long> mutableTriple : NETWORK_NODE.getAddressPortThroughPut()) {
            if (remoteAddress.equals(mutableTriple.getLeftCentre()) && Integer.valueOf(remotePort).equals(mutableTriple.getRightCentre())) {
                mutableTriple.setRight(mutableTriple.getRight() + len);
                return;
            }
        }
        Quadruple<Integer, String, Integer, Long> addressPortThroughPut = new Quadruple<>(localPort, remoteAddress, remotePort, (long) len);
        NETWORK_NODE.getAddressPortThroughPut().add(addressPortThroughPut);
    }

    @SuppressWarnings("unused")
    public static void collectInputTraffic(final Socket socket, final byte[] bytes, final int off, final int len) {
        if (log) {
            System.out.println("Reaper : Socket input - " + socket + ", length : " + len + ", offset : " + off/* + ":" + Arrays.toString(bytes)*/);
        }
    }

}