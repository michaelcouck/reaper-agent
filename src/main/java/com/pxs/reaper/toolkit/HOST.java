package com.pxs.reaper.toolkit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import sun.net.util.IPAddressUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.UUID;

/**
 * This class looks through the {@link InetAddress}(s) of the host to try find the 'unique' ip address
 * of the machine, or the HOSTNAME which hopefully is also unique on the network. Also iterates through the
 * {@link NetworkInterface}(s) of the local machine, finding the ip addresses bound to them.
 * <p>
 * Problem statement:
 * <pre>
 *     * IP address can be specified, and or spoofd
 *     * There are several ip addresses bound to each interface potentially
 *     * IP addresses need not be unique on the network, normally yes, but not necessarily
 * </pre>
 * <p>
 * Ergo, this is a best effort to identify the 'best' ip address/HOSTNAME.
 */
@Slf4j
public class HOST {

    /**
     * Sored static iip address for further access.
     */
    private static String HOSTNAME;

    /**
     * Finds the best ip address for this machine.
     */
    public static String hostname() {
        if (StringUtils.isNotEmpty(HOSTNAME)) {
            return HOSTNAME;
        }

        // Iterate over the interfaces and find an ip address that is not loopback
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            throw new RuntimeException("Couldn't access the interfaces of this machine : ");
        }
        networkInterfaces(networkInterfaces);
        if (StringUtils.isEmpty(HOSTNAME)) {
            // Try the OpenShift host name environment variable
            HOSTNAME = System.getProperty("HOSTNAME", null);
            if (StringUtils.isEmpty(HOSTNAME)) {
                try {
                    // Return a uuid that is at least unique to this vm/pod
                    HOSTNAME = UUID.fromString(InetAddress.getLocalHost().getCanonicalHostName()).toString();
                } catch (final UnknownHostException e) {
                    log.warn("Couldn't find internet address : ", e);
                    HOSTNAME = UUID.randomUUID().toString();
                }
            }
        }
        return HOSTNAME;
    }

    @SuppressWarnings("WeakerAccess")
    static void networkInterfaces(final Enumeration<NetworkInterface> networkInterfaces) {
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            // Exclude Docker and VMWare interfaces
            boolean docker = networkInterface.getDisplayName().startsWith("doc");
            boolean vmware = networkInterface.getDisplayName().startsWith("vmn");
            boolean virtual = networkInterface.getDisplayName().startsWith("virbr");
            if (docker || vmware || virtual) {
                continue;
            }
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            inetAddresses(inetAddresses);
            if (StringUtils.isNotEmpty(HOSTNAME)) {
                // Check if this is the ip address and not the hostname, i.e. laptop
                if (networkInterfaces.hasMoreElements() && !IPAddressUtil.isIPv4LiteralAddress(HOSTNAME)) {
                    continue;
                }
                return;
            }
        }
    }

    @SuppressWarnings("WeakerAccess")
    static void inetAddresses(final Enumeration<InetAddress> inetAddresses) {
        while (inetAddresses.hasMoreElements()) {
            InetAddress inetAddress = inetAddresses.nextElement();
            String hostAddress = inetAddress.getHostAddress();
            log.info("Host address : {}", hostAddress);
            // Exclude 127... localhost and loopback
            if (hostAddress.startsWith("127")) {
                continue;
            }
            if (!IPAddressUtil.isIPv4LiteralAddress(hostAddress)) {
                continue;
            }
            // Select the first address that is IPV4
            HOSTNAME = hostAddress;
            return;
        }
    }

}
