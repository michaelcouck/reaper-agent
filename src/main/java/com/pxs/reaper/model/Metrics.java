package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperic.sigar.*;

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

    /**
     * Model objects from the JVM management beans.
     */
    private MemoryPool metaspace;
    private MemoryPool psOldGen;
    private MemoryPool psEdenSpace;
    private MemoryPool codeCache;
    private MemoryPool compressedClassSpace;
    private MemoryPool psSurvivorSpace;

    private BufferPool mapped;
    private BufferPool bufferPoolMXBean;

    private ClassPool classPool;
    private ThreadPool threadPool;
    private GarbageCollection garbageCollection;

    /*@JsonPOJOBuilder(withPrefix = "")
    public static final class MetricsBuilder {
    }*/

}