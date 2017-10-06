package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;

import java.net.InetAddress;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metrics {

    /**
     * Ip address of the local agent.
     */
    private InetAddress inetAddress;

    /**
     * Model objects from Sigar that can be used directly, i.e. transported over the wire
     */
    private Cpu cpu;
    private CpuPerc cpuPerc;
    private Swap swap;
    private double[] loadAverage;
    private Mem mem;
    private NetInfo netInfo;
    private NetStat netStat;
    private ProcStat procStat;
    private Tcp tcp;

    /**
     * Jvm metrics in maps of maps.
     */
    private Map<Object, Object> attributes;

}