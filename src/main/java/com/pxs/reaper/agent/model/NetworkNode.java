package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.TreeSet;

@Setter
@Getter
public class NetworkNode {

    private long output;

    private int localPort;
    private String localAddress;

    private Set<Integer> remotePorts = new TreeSet<>();
    private Set<String> remoteAddresses = new TreeSet<>();

}