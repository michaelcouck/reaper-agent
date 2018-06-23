package com.pxs.reaper.agent.action.instrumentation;

import com.pxs.reaper.agent.model.NetworkNode;
import com.pxs.reaper.agent.model.Quadruple;
import com.pxs.reaper.agent.toolkit.HOST;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.WeakHashMap;

public class NetworkTrafficCollector {

    public static boolean log = Boolean.FALSE;

    public static final NetworkNode NETWORK_NODE = new NetworkNode();

    private static final WeakHashMap<Integer, Integer> LOCAL_SOCKET_PORT = new WeakHashMap<>();
    private static final WeakHashMap<Integer, Integer> REMOTE_SOCKET_PORT = new WeakHashMap<>();
    private static final WeakHashMap<Integer, String> SOCKET_ADDRESS = new WeakHashMap<>();

    static {
        try {
            NETWORK_NODE.setLocalAddress(HOST.hostname());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static void collectTraffic(final Socket socket, final int len) {
        if (log) {
            System.out.println("Reaper : Socket output - " + socket + ", length : " + len);
        }

        int socketHash = socket.hashCode();

        Integer localPort;
        Integer remotePort;
        String remoteAddress;

        if (!SOCKET_ADDRESS.containsKey(socketHash)) {
            InetSocketAddress localInetSocketAddress = (InetSocketAddress) socket.getLocalSocketAddress();
            InetSocketAddress remoteInetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

            localPort = localInetSocketAddress.getPort();
            remotePort = remoteInetSocketAddress.getPort();
            remoteAddress = remoteInetSocketAddress.getAddress().getHostAddress();

            collectTraffic(socketHash, localPort, remotePort, remoteAddress, len);
        }

        localPort = LOCAL_SOCKET_PORT.get(socketHash);
        remotePort = REMOTE_SOCKET_PORT.get(socketHash);
        remoteAddress = SOCKET_ADDRESS.get(socketHash);

        collectTraffic(socketHash, localPort, remotePort, remoteAddress, len);
    }

    @SuppressWarnings("WeakerAccess")
    public static void collectTraffic(final Integer socketHash, final Integer localPort, final Integer remotePort, final String remoteAddress, final long len) {
        if (localPort == null || remotePort == null || remoteAddress == null) {
            System.out.println("Null pointers, socket : " + socketHash + ", local port : " + localPort + ", remote port : " + remotePort + ", length : " + len);
            return;
        }

        if (!SOCKET_ADDRESS.containsKey(socketHash)) {
            LOCAL_SOCKET_PORT.put(socketHash, localPort);
            REMOTE_SOCKET_PORT.put(socketHash, remotePort);
            SOCKET_ADDRESS.put(socketHash, remoteAddress);
        }

        for (final Quadruple<Integer, String, Integer, Long> mutableTriple : NETWORK_NODE.getAddressPortThroughPut()) {
            if (remoteAddress.equals(mutableTriple.getLeftCentre()) && remotePort.equals(mutableTriple.getRightCentre())) {
                mutableTriple.setRight(mutableTriple.getRight() + len);
                return;
            }
        }

        Quadruple<Integer, String, Integer, Long> addressPortThroughPut = new Quadruple<>(localPort, remoteAddress, remotePort, len);
        NETWORK_NODE.getAddressPortThroughPut().add(addressPortThroughPut);
    }

}