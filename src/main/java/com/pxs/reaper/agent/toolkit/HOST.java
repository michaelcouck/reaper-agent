package com.pxs.reaper.agent.toolkit;

import org.apache.commons.lang.StringUtils;
import sun.net.util.IPAddressUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Logger;

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
public class HOST {

    private static Logger log = Logger.getLogger(HOST.class.getSimpleName());

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
        Collection<String> ipAddresses = ipAddressesForLocalHost(networkInterfaces);
        ipAddressForLocalHost(ipAddresses);
        if (StringUtils.isEmpty(HOSTNAME)) {
            // Try the OpenShift host name environment variable
            HOSTNAME = System.getProperty("HOSTNAME", null);
            if (StringUtils.isEmpty(HOSTNAME)) {
                try {
                    // Return a uuid that is at least unique to this vm/pod
                    HOSTNAME = UUID.fromString(InetAddress.getLocalHost().getCanonicalHostName()).toString();
                } catch (final UnknownHostException e) {
                    log.warning("Couldn't find internet address : " + e);
                    HOSTNAME = UUID.randomUUID().toString();
                }
            }
        }
        return HOSTNAME;
    }

    public static List<String> ipAddressesForLocalHost(final Enumeration<NetworkInterface> networkInterfaces) {
        List<String> ipAddresses = new ArrayList<>();
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
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                String ipAddress = inetAddress.getHostAddress();
                ipAddresses.add(ipAddress);
            }
        }
        log.info("Host names : " + ipAddresses);
        return ipAddresses;
    }

    private static void ipAddressForLocalHost(final Collection<String> ipAddresses) {
        for (final String ipAddress : ipAddresses) {
            // Exclude 127... localhost and loopback
            if (ipAddress.startsWith("127")) {
                continue;
            }
            if (!IPAddressUtil.isIPv4LiteralAddress(ipAddress)) {
                continue;
            }
            // Select the first address that is IPV4
            HOSTNAME = ipAddress;
            return;
        }
    }

}
