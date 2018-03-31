package com.pxs.reaper.agent.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;

@Getter
@Setter
@ToString
@SuppressWarnings("WeakerAccess")
public abstract class Metrics {

    public Metrics() {
        setCreated(System.currentTimeMillis());
        setCodeBase(FilenameUtils.getName(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()));
    }

    protected String id;

    private String codeBase;

    private String type = this.getClass().getName();

    /**
     * Ip address of the local agent.
     */
    private String ipAddress;

    private long created;

}
