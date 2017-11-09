package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.pxs.reaper.model.converter.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

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

@Entity
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class OSMetrics extends Metrics {

    @Column
    private String type = "com.pxs.reaper.model.OSMetrics";

    /**
     * Model objects from Sigar that can be used directly, i.e. transported over the wire
     */
    @Convert(converter = CpuArrayConverter.class)
    private Cpu[] cpu;
    @Convert(converter = CpuPercArrayConverter.class)
    private CpuPerc[] cpuPerc;
    @Convert(converter = CpuInfoArrayConverter.class)
    private CpuInfo[] cpuInfo;
    @Convert(converter = DoubleArrayConverter.class)
    private double[] loadAverage;

    @Convert(converter = TcpConverter.class)
    private Tcp tcp;
    @Convert(converter = MemConverter.class)
    private Mem mem;
    @Convert(converter = SwapConverter.class)
    private Swap swap;
    @Convert(converter = NetInfoConverter.class)
    private NetInfo netInfo;
    @Convert(converter = NetStatConverter.class)
    private NetStat netStat;
    @Convert(converter = NetRouteArrayConverter.class)
    private NetRoute[] netRoutes;
    @Convert(converter = NetConnectionArrayConverter.class)
    private NetConnection[] netConnections;
    @Convert(converter = ProcStatConverter.class)
    private ProcStat procStat;
    @Convert(converter = ResourceLimitConverter.class)
    private ResourceLimit resourceLimit;

}