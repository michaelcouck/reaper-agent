package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.SortedSet;
import java.util.TreeSet;

@Setter
@Getter
public class NetworkNode {

    private String localAddress;

    private SortedSet<Quadruple<Integer, String, Integer, Long>> addressPortThroughPut = new TreeSet<>();

}