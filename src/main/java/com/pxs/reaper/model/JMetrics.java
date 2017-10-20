package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
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
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JMetrics {

    /**
     * Ip address of the local agent.
     */
    private InetAddress inetAddress;

    /**
     * The PID/name of the JVM.
     */
    private String pid;

    private Date date;

    private Classloading classLoading;
    private Compilation compilation;
    private GarbageCollection[] garbageCollection;
    private Memory memory;
    private MemoryPool[] memoryPools;
    private Threading threading;

}
