package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;
import java.util.Date;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JMetrics extends Metrics {

    private String type = "com.pxs.reaper.model.JMetrics";

    /**
     * The PID/name of the JVM.
     */
    private String pid;

    private Memory memory;
    private Threading threading;
    private Compilation compilation;
    private MemoryPool[] memoryPools;
    private Classloading classLoading;
    private GarbageCollection[] garbageCollection;

}
