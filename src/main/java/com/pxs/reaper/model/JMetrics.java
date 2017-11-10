package com.pxs.reaper.model;

import com.couchbase.client.java.repository.annotation.Field;
import com.couchbase.client.java.repository.annotation.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.couchbase.core.mapping.Document;

/**
 * Parent class for all Java process telemetry, the memory, the threads etc.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Getter
@Setter
@Document
public class JMetrics {

    @Id
    protected String id;

    @Field
    private String type = this.getClass().getName();

    /**
     * Ip address of the local agent.
     */
    @Field
    private String ipAddress;

    @Field
    private long created;

    /**
     * The PID/name of the JVM.
     */
    @Field
    private String pid;

    @Field
    private Memory memory;
    @Field
    private Threading threading;
    @Field
    private Compilation compilation;
    @Field
    private MemoryPool[] memoryPools;
    @Field
    private Classloading classLoading;
    @Field
    private GarbageCollection[] garbageCollection;

}
