package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;

import java.net.InetAddress;
import java.util.Date;

/**
 * Contains operating system metrics, low level.
 * <p>
 * Potentially interesting additions to the metrics.
 * <pre>
 *     {@link NetConnection}
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OSMetrics {

    private String type = "com.pxs.reaper.model.OSMetrics";

    /**
     * Ip address of the local agent.
     */
    private InetAddress inetAddress;

    /**
     * Time stamp at the time of collection.
     */
    private Date date;

    /**
     * Model objects from Sigar that can be used directly, i.e. transported over the wire
     */
    private Cpu[] cpu;
    private CpuPerc[] cpuPerc;
    private CpuInfo[] cpuInfo;

    private double[] loadAverage;

    private Tcp tcp;
    private Mem mem;
    private Swap swap;
    private NetInfo netInfo;
    private NetStat netStat;
    private NetRoute[] netRoutes;
    private NetConnection[] netConnections;
    private ProcStat procStat;
    private ResourceLimit resourceLimit;

}