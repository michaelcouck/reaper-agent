package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pxs.reaper.model.converter.MemoryUsageConverter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Convert;
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

    @Column
    private String name;
    @Column
    private long usageThreshold;

    @Column
    private long usageThresholdCount;
    @Column
    private long collectionUsageThreshold;
    @Column
    private long collectionUsageThresholdCount;

    private MemoryType type;
    @Convert(converter = MemoryUsageConverter.class)
    private MemoryUsage usage;
    @Convert(converter = MemoryUsageConverter.class)
    private MemoryUsage peakUsage;
    @Convert(converter = MemoryUsageConverter.class)
    private MemoryUsage collectionUsage;

}
