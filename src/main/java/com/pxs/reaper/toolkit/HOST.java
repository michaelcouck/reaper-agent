package com.pxs.reaper.toolkit;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * This class looks through the {@link InetAddress}(s) of the host to try find the 'unique' ip address
 * of the machine, or the hostname which hopefully is also unique on the network. Also iterates through the
 * {@link NetworkInterface}(s) of the local machine, finding the ip addresses bound to them.
 * <p>
 * Problem statement:
 * <pre>
 *     * IP address can be specified, and or spoofd
 *     * There are several ip addresses bound to each interface potentially
 *     * IP addresses need no be unique on the network, normally yes, but not necessarily
 * </pre>
 * <p>
 * Ergo, this is a best effort to identify the 'best' ip address/hostname.
 */
@Slf4j
public class HOST {

    /**
     * The pattern for ip addresses, i.e. 192.168.1.0 etc.
     */
    private static Pattern ipPattern = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    private static String hostname;

    public static String hostname() {
        if (StringUtils.isNotEmpty(hostname)) {
            return hostname;
        }
        // Try the OpenShift host name environment variable
        hostname = System.getProperty("HOSTNAME", null);
        if (StringUtils.isNotEmpty(hostname)) {
            return hostname;
        }

        // Iterate over the interfaces and find an ip address that is not loopback
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            throw new RuntimeException("Couldn't access the interfaces of this machine : ");
        }

        hostname = networkInterfaces(networkInterfaces);
        if (StringUtils.isNotEmpty(hostname)) {
            return hostname;
        } else {
            // Return a uuid that is at least unique to this vm/pod
            try {
                return hostname = UUID.fromString(InetAddress.getLocalHost().getCanonicalHostName()).toString();
            } catch (final UnknownHostException e) {
                log.warn("Couldn't find internet address : ", e);
                return hostname = UUID.randomUUID().toString();
            }
        }
    }

    static String networkInterfaces(final Enumeration<NetworkInterface> networkInterfaces) {
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            // Exclude Docker and VMWare interfaces
            if (networkInterface.getDisplayName().startsWith("doc") ||
                    networkInterface.getDisplayName().startsWith("vmn")) {
                continue;
            }
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            hostname = inetAddresses(inetAddresses);
            if (StringUtils.isNotEmpty(hostname)) {
                return hostname;
            }
        }
        return null;
    }

    static String inetAddresses(final Enumeration<InetAddress> inetAddresses) {
        while (inetAddresses.hasMoreElements()) {
            InetAddress inetAddress = inetAddresses.nextElement();
            String hostaddress = inetAddress.getHostAddress();

            log.info("Host address : {}", new Object[]{hostaddress});

            // Exclude anything that is mac address, hardware and ipv6
            if (!ipPattern.matcher(hostaddress).matches()) {
                continue;
            }
            // Exclude 127... localhost and loopback
            if (hostaddress.startsWith("127")) {
                continue;
            }
            return hostname = hostaddress;
        }
        return null;
    }

}