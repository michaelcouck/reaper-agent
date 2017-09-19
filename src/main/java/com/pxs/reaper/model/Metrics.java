package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;

import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metrics {

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

    private Map<Object, Object> attributes;

}