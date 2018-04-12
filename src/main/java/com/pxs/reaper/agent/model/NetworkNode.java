package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class NetworkNode {

    private long output;

    private int localPort;
    private String localAddress;

    private List<Integer> remotePorts = new ArrayList<>();
    private List<String> remoteAddresses = new ArrayList<>();

}
