package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NetworkNode {

    private long output;

    private int localPort;
    private String localAddress;

    private int remotePort;
    private String remoteAddress;

}
