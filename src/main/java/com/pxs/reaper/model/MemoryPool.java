package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;

@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemoryPool {

    private String name;
    private MemoryType type;

    private long usageThreshold;
    private long usageThresholdCount;
    private long collectionUsageThreshold;
    private long collectionUsageThresholdCount;

    private MemoryUsage usage;
    private MemoryUsage peakUsage;
    private MemoryUsage collectionUsage;

}
