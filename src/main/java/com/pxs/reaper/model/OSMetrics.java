package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
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