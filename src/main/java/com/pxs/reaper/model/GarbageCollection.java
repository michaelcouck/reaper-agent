package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains garbage collection metrics, how many, time taken. Will be correlated to performance and
 * memory allocation and leakage.
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
public class GarbageCollection {

    private String name;
    private long collectionCount;
    private long collectionTime;

}
