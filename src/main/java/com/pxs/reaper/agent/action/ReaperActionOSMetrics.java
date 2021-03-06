package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.Constant;
import com.pxs.reaper.agent.model.OSMetrics;
import com.pxs.reaper.agent.toolkit.HOST;
import com.pxs.reaper.agent.toolkit.OS;
import com.pxs.reaper.agent.transport.Transport;
import org.hyperic.sigar.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * The action will inspect the local operating system for metrics and telemetry data, populate an {@link OSMetrics} object
 * with the results from the various metrics available, and post the json converted string to the central analyzer for building
 * models.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
public class ReaperActionOSMetrics extends AReaperActionMetrics {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    private boolean permissionDenied = Boolean.FALSE;

    /**
     * {@link Sigar} is the native operating system access to metrics and telemetry. It provides {@link Cpu},
     * {@link CpuInfo}, {@link Mem} information and several other metrics, some of which are quite low level.
     */
    private Sigar sigar;
    /**
     * Proxy class for {@link Sigar} for simplifying the refresh rate of the gathering of data
     */
    private SigarProxy sigarProxy;
    /**
     * Transport of the data over the wire.
     */
    private Transport transport = Constant.TRANSPORT;

    public ReaperActionOSMetrics() {
        init();
        log.info("Attached to operating system : " + HOST.hostname());
    }

    private void init() {
        sigar = new Sigar();
        sigarProxy = SigarProxyCache.newInstance(sigar, 1000);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            // Gather all the operating system metrics, pop them in a OSMetrics object and post them
            OSMetrics osMetrics = getMetrics();
            // log.info("Posting OS metrics : " + osMetrics);
            transport.postMetrics(osMetrics);
        } catch (final Exception e) {
            log.log(Level.SEVERE, e.getMessage(), e);
            terminate();
            init();
        } finally {
            SigarProxyCache.clear(sigarProxy);
        }
    }

    public OSMetrics getMetrics() throws SigarException {
        OSMetrics osMetrics = new OSMetrics();

        common(osMetrics);
        networkThroughput(osMetrics);

        Cpu[] cpu = cpu(sigarProxy);
        CpuInfo[] cpuInfo = cpuInfo(sigarProxy);
        CpuPerc[] cpuPerc = cpuPerc(sigarProxy);
        Swap swap = swap(sigarProxy);
        double[] loadAverage = loadAverage(sigarProxy);
        Mem mem = mem(sigarProxy);
        NetInfo netInfo = netInfo(sigarProxy);
        NetStat netStat = netStat(sigarProxy);
        NetRoute[] netRoutes = netRoutes(sigarProxy);
        NetConnection[] netConnections = netConnections(sigarProxy);
        ProcStat procStat = procStat(sigarProxy);
        Tcp tcp = tcp(sigarProxy);
        ResourceLimit resourceLimit = resourceLimit(sigarProxy);
        String[] networkInterfaces = getNetworkInterfaces(sigarProxy);
        NetInterfaceStat[] netInterfaceStats = getNetInterfaceStat(sigarProxy);
        DiskUsage[] diskUsages = getDiskUsage(sigarProxy);
        FileSystemUsage[] fileSystemUsages = getFileSystemUsage(sigarProxy);
        // OperatingSystem operatingSystem = getOperatingSystem();

        osMetrics.setCpu(cpu);
        osMetrics.setCpuPerc(cpuPerc);
        osMetrics.setCpuInfo(cpuInfo);
        osMetrics.setLoadAverage(loadAverage);

        osMetrics.setNetInfo(netInfo);
        osMetrics.setNetStat(netStat);
        osMetrics.setNetRoutes(netRoutes);
        osMetrics.setNetConnections(netConnections);
        osMetrics.setTcp(tcp);

        osMetrics.setMem(mem);
        osMetrics.setSwap(swap);
        osMetrics.setProcStat(procStat);
        osMetrics.setResourceLimit(resourceLimit);
        osMetrics.setNetworkInterfaces(networkInterfaces);
        osMetrics.setNetInterfaceStats(netInterfaceStats);
        osMetrics.setDiskUsages(diskUsages);
        osMetrics.setFileSystemUsages(fileSystemUsages);
        // osMetrics.setOperatingSystem(operatingSystem);

        return osMetrics;
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

    private NetRoute[] netRoutes(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getNetRouteList();
    }

    private NetConnection[] netConnections(final SigarProxy sigarProxy) throws SigarException {
        int flags = NetFlags.CONN_SERVER | NetFlags.CONN_CLIENT | NetFlags.CONN_TCP;
        return sigarProxy.getNetConnectionList(flags);
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

    private String[] getNetworkInterfaces(final SigarProxy sigarProxy) throws SigarException {
        return sigarProxy.getNetInterfaceList();
    }

    private NetInterfaceStat[] getNetInterfaceStat(final SigarProxy sigarProxy) throws SigarException {
        List<NetInterfaceStat> netInterfaceStats = new ArrayList<>();
        String[] networkInterfaces = getNetworkInterfaces(sigarProxy);
        Stream.of(networkInterfaces).forEach(networkInterface -> {
            try {
                NetInterfaceStat netInterfaceStat = sigarProxy.getNetInterfaceStat(networkInterface);
                netInterfaceStats.add(netInterfaceStat);
            } catch (final SigarException e) {
                log.log(Level.INFO, "Error getting interface statistics : ", e);
            }
        });
        return netInterfaceStats.toArray(new NetInterfaceStat[netInterfaceStats.size()]);
    }

    private DiskUsage[] getDiskUsage(final SigarProxy sigarProxy) throws SigarException {
        List<DiskUsage> diskUsages = new ArrayList<>();
        FileSystem[] fileSystems = sigarProxy.getFileSystemList();
        for (final FileSystem fileSystem : fileSystems) {
            if (fileSystem.getType() == 1) {
                continue;
            }
            // System.out.println("Name : " + fileSystem.getDirName() + ":" + fileSystem.getType());
            try {
                DiskUsage diskUsage = sigarProxy.getDiskUsage(fileSystem.getDirName());
                diskUsages.add(diskUsage);
            } catch (final Exception e) {
                log.severe("Exception getting disk usage : " + fileSystem.getDirName() + ", message :" + e.getMessage());
            }
        }
        return diskUsages.toArray(new DiskUsage[diskUsages.size()]);
    }

    private FileSystemUsage[] getFileSystemUsage(final SigarProxy sigarProxy) throws SigarException {
        List<FileSystemUsage> fileSystemUsages = new ArrayList<>();
        if (permissionDenied) {
            return fileSystemUsages.toArray(new FileSystemUsage[fileSystemUsages.size()]);
        }
        FileSystem[] fileSystems = sigarProxy.getFileSystemList();
        for (final FileSystem fileSystem : fileSystems) {
            try {
                FileSystemUsage fileSystemUsage = sigarProxy.getFileSystemUsage(fileSystem.getDirName());
                fileSystemUsages.add(fileSystemUsage);
            } catch (final Exception e) {
                log.info("Exception accessing the file system : " + e.getMessage());
                if (e.getMessage().contains("Permission denied")) {
                    permissionDenied = Boolean.TRUE;
                }
            }
        }
        return fileSystemUsages.toArray(new FileSystemUsage[fileSystemUsages.size()]);
    }

    @SuppressWarnings("unused")
    private OperatingSystem getOperatingSystem() {
        return OperatingSystem.getInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean terminate() {
        try {
            sigar.close();
            log.info("Closed sigar : " + sigar);
        } catch (final Exception e) {
            log.log(Level.SEVERE, "Exception closing the Sigar : ", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

}