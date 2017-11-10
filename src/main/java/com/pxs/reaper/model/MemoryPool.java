package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;

/**
 * Contains memory pool telemetry, like scavenge time and released memory blocks.
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
public class MemoryPool {

    private String name;
    private long usageThreshold;

    private long usageThresholdCount;
    private long collectionUsageThreshold;
    private long collectionUsageThresholdCount;

    private MemoryType type;
    private MemoryUsage usage;
    private MemoryUsage peakUsage;
    private MemoryUsage collectionUsage;

}
