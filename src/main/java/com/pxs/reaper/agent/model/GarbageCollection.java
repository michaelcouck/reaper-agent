package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
@ToString
@NoArgsConstructor
public class GarbageCollection {

    private String name;
    private long collectionCount;
    private long collectionTime;

}
