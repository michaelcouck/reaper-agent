package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.hyperic.sigar.*;

@Getter
@Builder
@ToString
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metrics {

    private Cpu cpu;
    private CpuPerc cpuPerc;
    private Swap swap;
    private double[] loadAverage;
    private Mem mem;
    private NetInfo netInfo;
    private NetStat netStat;
    private ProcStat procStat;
    private Tcp tcp;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class MetricsBuilder {
    }

}
