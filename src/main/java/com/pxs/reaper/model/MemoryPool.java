package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemoryPool {

    private long peakUsage;
    private long collectionUsage;
    private long collectionUsageThreshold;
    private long collectionUsageThresholdCount;

    private long max;
    private long used;
    private long init;
    private long committed;

    private long usageThreshold;
    private long usageThresholdCount;
    private long usageThresholdExceeded;

    private List<MemoryType> memoryTypes = new ArrayList<>();

}
