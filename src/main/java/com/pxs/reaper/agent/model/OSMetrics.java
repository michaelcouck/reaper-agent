package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;
import org.hyperic.sigar.OperatingSystem;

/**
 * Contains operating system metrics, low level.
 * <p>
 * Potentially interesting additions to the metrics.
 * <pre>
 *     {@link FileSystemUsage}
 *     {@link DiskUsage}
 * </pre>
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class OSMetrics extends Metrics {

    /**
     * Cpu information
     */
    private Cpu[] cpu;
    private CpuPerc[] cpuPerc;
    private CpuInfo[] cpuInfo;
    private ProcStat procStat;
    private double[] loadAverage;

    /**
     * Network and traffic information
     */
    private Tcp tcp;
    private NetInfo netInfo;
    private NetStat netStat;
    private NetRoute[] netRoutes;
    private String[] networkInterfaces;
    private NetConnection[] netConnections;
    private NetInterfaceStat[] netInterfaceStats;

    /**
     * Memory information
     */
    private Mem mem;
    private Swap swap;

    /**
     * Disk information
     */
    private DiskUsage[] diskUsages;
    private FileSystemUsage[] fileSystemUsages;

    /**
     * Operating system information, including system limits
     */
    private ResourceLimit resourceLimit;
    private OperatingSystem operatingSystem;

}