package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.pxs.reaper.model.converter.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
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

    @Convert(converter = MemoryConverter.class)
    private Memory memory;
    @Convert(converter = ThreadingConverter.class)
    private Threading threading;
    @Convert(converter = CompilationConverter.class)
    private Compilation compilation;
    @Convert(converter = MemoryPoolArrayConverter.class)
    private MemoryPool[] memoryPools;
    @Convert(converter = ClassloadingConverter.class)
    private Classloading classLoading;
    @Convert(converter = GarbageCollectionArrayConverter.class)
    private GarbageCollection[] garbageCollection;

}
