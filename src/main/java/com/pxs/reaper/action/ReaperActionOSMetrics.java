package com.pxs.reaper.action;

import com.pxs.reaper.Constant;
import com.pxs.reaper.model.OSMetrics;
import com.pxs.reaper.transport.Transport;
import com.pxs.reaper.transport.WebSocketTransport;
import ikube.toolkit.OS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hyperic.sigar.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The action will inspect the local operating system for metrics and telemetry data, populate an {@link OSMetrics} object
 * with the results from the various metrics available, and post the json converted string to the central analyzer for building
 * models.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Slf4j
public class ReaperActionOSMetrics extends TimerTask implements ReaperAction {

    /**
     * The pattern for ip addresses, i.e. 192.168.1.0 etc.
     */
    private static final Pattern IP_PATTERN;

    static {
        IP_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
    }

    /**
     * The unique ip, hostname or UUID unique to this pod
     */
    private String hostName;
    /**
     * {@link Sigar} is the native operating system access to metrics and telemetry. It provides {@link Cpu},
     * {@link CpuInfo}, {@link Mem} information and several other metrics, some of which are quite low level.
     */
    private final Sigar sigar;
    /**
     * Provides transport of the metrics from the class to the central analyzer over the wire
     */
    private final Transport transport;
    /**
     * Proxy class for {@link Sigar} for simplifying the refresh rate of the gathering of data
     */
    private final SigarProxy sigarProxy;

    public ReaperActionOSMetrics() {
        sigar = new Sigar();
        transport = new WebSocketTransport();
        sigarProxy = SigarProxyCache.newInstance(sigar, 1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            // Gather all the operating system metrics, pop them in a OSMetrics object and post them
            OSMetrics osMetrics = OSMetrics.builder().build();
            InetAddress inetAddress = InetAddress.getByName(getHostname());
            osMetrics.setInetAddress(inetAddress);

            synchronized (sigar) {
                Cpu[] cpu = cpu(sigarProxy);
                CpuInfo[] cpuInfo = cpuInfo(sigarProxy);
                CpuPerc[] cpuPerc = cpuPerc(sigarProxy);
                Swap swap = swap(sigarProxy);
                double[] loadAverage = loadAverage(sigarProxy);
                Mem mem = mem(sigarProxy);
                NetInfo netInfo = netInfo(sigarProxy);
                NetStat netStat = netStat(sigarProxy);
                ProcStat procStat = procStat(sigarProxy);
                Tcp tcp = tcp(sigarProxy);
                ResourceLimit resourceLimit = resourceLimit(sigarProxy);

                osMetrics.setCpu(cpu);
                osMetrics.setCpuPerc(cpuPerc);
                osMetrics.setCpuInfo(cpuInfo);

                osMetrics.setLoadAverage(loadAverage);
                osMetrics.setMem(mem);
                osMetrics.setNetInfo(netInfo);
                osMetrics.setNetStat(netStat);
                osMetrics.setProcStat(procStat);
                osMetrics.setSwap(swap);
                osMetrics.setTcp(tcp);
                osMetrics.setResourceLimit(resourceLimit);

                transport.postMetrics(osMetrics);
            }
        } catch (final SigarException | UnknownHostException e) {
            throw new RuntimeException(e);
        } finally {
            SigarProxyCache.clear(sigarProxy);
        }
    }

    private Tcp tcp(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getTcp();
    }

    private ProcStat procStat(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getProcStat();
    }

    private NetStat netStat(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getNetStat();
    }

    private NetInfo netInfo(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getNetInfo();
    }

    private Mem mem(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getMem();
    }

    private double[] loadAverage(final SigarProxy sigarProxy) throws SigarException {
        if (OS.isOs("Linux")) {
            return sigarProxy.getLoadAverage();
        }
        return new double[0];
    }

    private CpuPerc[] cpuPerc(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getCpuPercList();
    }

    private Cpu[] cpu(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getCpuList();
    }

    private CpuInfo[] cpuInfo(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getCpuInfoList();
    }

    private Swap swap(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getSwap();
    }

    private ResourceLimit resourceLimit(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getResourceLimit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean terminate() {
        synchronized (sigar) {
            try {
                sigar.close();
            } catch (final Exception e) {
                log.error("Exception closing the Sigar : ", e);
            }
        }
        boolean terminated = cancel();
        Constant.TIMER.purge();
        return terminated;
    }

    String getHostname() throws UnknownHostException {
        if (StringUtils.isNotEmpty(hostName)) {
            return hostName;
        }
        // Try the OpenShift host name environment variable
        hostName = System.getProperty("HOSTNAME", null);
        if (StringUtils.isNotEmpty(hostName)) {
            return hostName;
        }

        // Iterate over the interfaces and find an ip address that is not loopback
        Enumeration<NetworkInterface> networkInterfaces;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (final SocketException e) {
            throw new RuntimeException("Couldn't access the interfaces of this machine : ");
        }
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            // Exclude Docker and VMWare interfaces
            if (networkInterface.getDisplayName().startsWith("doc") ||
                    networkInterface.getDisplayName().startsWith("vmn")) {
                continue;
            }
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                String hostAddress = inetAddress.getHostAddress();

                log.debug("Host address : {}", new Object[]{hostAddress});

                // Exclude anything that is mac address, hardware and ipv6
                if (!IP_PATTERN.matcher(hostAddress).matches()) {
                    continue;
                }
                // Exclude 127... localhost and loopback
                if (hostAddress.startsWith("127")) {
                    continue;
                }
                return hostAddress;
            }
        }
        // Return a uuid that is at least unique to this vm/pod
        return UUID.fromString(InetAddress.getLocalHost().getCanonicalHostName()).toString();
    }
}