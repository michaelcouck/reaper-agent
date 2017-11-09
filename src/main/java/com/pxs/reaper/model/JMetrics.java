package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlRootElement;

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

@Entity
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class JMetrics extends Metrics {

    @Column
    private String type = "com.pxs.reaper.model.JMetrics";

    /**
     * The PID/name of the JVM.
     */
    @Column
    private String pid;

    private Memory memory;
    private Threading threading;
    private Compilation compilation;
    private MemoryPool[] memoryPools;
    private Classloading classLoading;
    private GarbageCollection[] garbageCollection;

}
