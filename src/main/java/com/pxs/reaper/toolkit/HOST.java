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
 * of the machine, or the HOSTNAME which hopefully is also unique on the network. Also iterates through the
 * {@link NetworkInterface}(s) of the local machine, finding the ip addresses bound to them.
 * <p>
 * Problem statement:
 * <pre>
 *     * IP address can be specified, and or spoofd
 *     * There are several ip addresses bound to each interface potentially
 *     * IP addresses need no be unique on the network, normally yes, but not necessarily
 * </pre>
 * <p>
 * Ergo, this is a best effort to identify the 'best' ip address/HOSTNAME.
 */
@Slf4j
public class HOST {

    /**
     * The pattern for ip addresses, i.e. 192.168.1.0 etc.
     */
    private static Pattern IP_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    private static String HOSTNAME;

    public static String hostname() {
        if (StringUtils.isNotEmpty(HOSTNAME)) {
            return HOSTNAME;
        }
        // Try the OpenShift host name environment variable
        HOSTNAME = System.getProperty("HOSTNAME", null);
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

        HOSTNAME = networkInterfaces(networkInterfaces);
        if (StringUtils.isNotEmpty(HOSTNAME)) {
            return HOSTNAME;
        } else {
            // Return a uuid that is at least unique to this vm/pod
            try {
                return HOSTNAME = UUID.fromString(InetAddress.getLocalHost().getCanonicalHostName()).toString();
            } catch (final UnknownHostException e) {
                log.warn("Couldn't find internet address : ", e);
                return HOSTNAME = UUID.randomUUID().toString();
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
            HOSTNAME = inetAddresses(inetAddresses);
            if (StringUtils.isNotEmpty(HOSTNAME)) {
                return HOSTNAME;
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
            if (!IP_PATTERN.matcher(hostaddress).matches()) {
                continue;
            }
            // Exclude 127... localhost and loopback
            if (hostaddress.startsWith("127")) {
                continue;
            }
            return HOSTNAME = hostaddress;
        }
        return null;
    }

}
