package com.pxs.reaper.agent.model;

// import com.couchbase.client.java.repository.annotation.Field;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;
import org.hyperic.sigar.OperatingSystem;
// import org.springframework.data.couchbase.core.mapping.Document;

/**
 * Contains operating system metrics, low level.
 * <p>
 * Potentially interesting additions to the metrics.
 * <pre>
 *     {@link NetInterfaceStat}
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
     * Model objects from Sigar that can be used directly, i.e. transported over the wire
     */
    private double[] loadAverage;

    private Cpu[] cpu;
    private CpuPerc[] cpuPerc;
    private CpuInfo[] cpuInfo;

    private Tcp tcp;
    private Mem mem;
    private Swap swap;
    private NetInfo netInfo;
    private NetStat netStat;
    private NetRoute[] netRoutes;
    private NetConnection[] netConnections;
    private ProcStat procStat;
    private ResourceLimit resourceLimit;
    private NetInterfaceStat[] netInterfaceStats;
    private OperatingSystem operatingSystem;

}