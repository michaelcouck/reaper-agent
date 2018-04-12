package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

@Getter
@Setter
@ToString
public abstract class Metrics {

    protected String id;

    private String codeBase;

    private String type = this.getClass().getName();

    /**
     * Ip address of the local agent.
     */
    private String ipAddress;

    private Collection<NetworkNode> networkNodes;

    private long created;

}