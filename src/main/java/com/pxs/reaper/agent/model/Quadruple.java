package com.pxs.reaper.agent.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.io.Serializable;
import java.util.Collections;

/**
 * @author Michael Couck
 * @version 1.0
 * @since 14-04-2018
 */
@Setter
@Getter
@AllArgsConstructor
public class Quadruple<A, B, C, D> implements Serializable, Comparable<Quadruple> {

    private A left; // Local port
    private B leftCentre; // Remote address
    private C rightCentre; // Remote port
    private D right; // Throughput


    @Override
    public int compareTo(final Quadruple o) {
        return CompareToBuilder.reflectionCompare(o, this, Collections.singletonList("right"));
    }

}