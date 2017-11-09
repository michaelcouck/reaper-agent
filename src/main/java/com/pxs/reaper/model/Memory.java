package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.management.MemoryUsage;

/**
 * Contains memory usage for the entire Java process.
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
public class Memory {

    private long objectPendingFinalizationCount;
    private MemoryUsage heapMemoryUsage;
    private MemoryUsage nonHeapMemoryUsage;

}
