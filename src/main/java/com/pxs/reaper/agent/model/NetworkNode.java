package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.util.SortedSet;
import java.util.TreeSet;

@Setter
@Getter
public class NetworkNode {

    private String localAddress;

    private SortedSet<MutableTriple<String, Integer, Long>> addressPortThroughPut = new TreeSet<>();

}