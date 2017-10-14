package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;
import java.util.Date;

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
