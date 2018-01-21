package com.pxs.reaper.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
// import org.springframework.data.couchbase.core.mapping.Document;

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
// @Document
@NoArgsConstructor
public class OperatingSystem {

    private String name;
    private String version;
    private String arch;
    private int availableProcessors;
    private double systemLoadAverage;

}
