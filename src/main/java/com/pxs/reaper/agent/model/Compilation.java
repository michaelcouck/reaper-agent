package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contains compilation time for the Jit compiler, probably not very highly correlated to performance, but could be.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Compilation {

    private long compilationTime;

}
