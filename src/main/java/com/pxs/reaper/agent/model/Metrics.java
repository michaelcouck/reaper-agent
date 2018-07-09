package com.pxs.reaper.agent.model;

import com.pxs.reaper.agent.Constant;
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
    private long metricsPostPeriod = Constant.WAIT_TO_POST_METRICS;

    /**
     * Ip address of the local agent.
     */
    private String ipAddress;

    private NetworkNode networkNode;

    private long created;

}