package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public abstract class Metrics {

    protected String id;
    private String type = this.getClass().getName();

    private String userDir;
    private String codeBase;

    /**
     * Ip address of the local agent.
     */
    private String ipAddress;

    private NetworkNode networkNode;

    private long created;

}