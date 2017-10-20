package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;

import java.net.InetAddress;

/**
 * Contains operating system metrics, low level.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OSMetrics {

    /**
     * Ip address of the local agent.
     */
    private InetAddress inetAddress;

    /**
     * Model objects from Sigar that can be used directly, i.e. transported over the wire
     */
    private Cpu[] cpu;
    private CpuPerc[] cpuPerc;
    private CpuInfo[] cpuInfo;

    private ResourceLimit resourceLimit;

    private double[] loadAverage;

    private Mem mem;
    private Swap swap;
    private Tcp tcp;
    private NetInfo netInfo;
    private NetStat netStat;
    private ProcStat procStat;

}