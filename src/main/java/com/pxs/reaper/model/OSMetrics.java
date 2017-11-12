package com.pxs.reaper.model;

import com.couchbase.client.java.repository.annotation.Field;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hyperic.sigar.*;
import org.springframework.data.couchbase.core.mapping.Document;

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
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OSMetrics extends Metrics {

    /**
     * Model objects from Sigar that can be used directly, i.e. transported over the wire
     */
    @Field
    private double[] loadAverage;

    @Field
    private Cpu[] cpu;
    @Field
    private CpuPerc[] cpuPerc;
    @Field
    private CpuInfo[] cpuInfo;

    @Field
    private Tcp tcp;
    @Field
    private Mem mem;
    @Field
    private Swap swap;
    @Field
    private NetInfo netInfo;
    @Field
    private NetStat netStat;
    @Field
    private NetRoute[] netRoutes;
    @Field
    private NetConnection[] netConnections;
    @Field
    private ProcStat procStat;
    @Field
    private ResourceLimit resourceLimit;

}