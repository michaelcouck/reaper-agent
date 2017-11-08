package com.pxs.reaper.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@SuppressWarnings("WeakerAccess")
public abstract class Metrics {

    /**
     * Ip address of the local agent.
     */
    private String ipAddress;

    /**
     * Time stamp at the time of collection.
     */
    private Date date;
}
