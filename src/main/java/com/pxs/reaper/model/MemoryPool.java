package com.pxs.reaper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.couchbase.core.mapping.Document;

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
@ToString
@Document
@NoArgsConstructor
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
