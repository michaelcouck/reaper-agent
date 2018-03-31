package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
@ToString
@NoArgsConstructor
public class Memory {

    private long maxMemory;
    private long freeMemory;
    private long totalMemory;
    private long objectPendingFinalizationCount;

    private MemoryUsage heapMemoryUsage;
    private MemoryUsage nonHeapMemoryUsage;

}