package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Parent class for all Java process telemetry, the memory, the threads etc.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class JMetrics extends Metrics {

    /**
     * The PID/name of the JVM.
     */
    private String pid;
    private long upTime;
    private long startTime;
    private short availableProcessors;

    private Memory memory;
    private Threading threading;
    private Compilation compilation;
    private MemoryPool[] memoryPools;
    private Classloading classLoading;
    private GarbageCollection[] garbageCollection;
    private OperatingSystem operatingSystem;

    private Map<String, Integer> exceptions;

}