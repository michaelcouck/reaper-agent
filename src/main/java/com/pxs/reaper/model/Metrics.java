package com.pxs.reaper.model;

import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.util.Date;

@Getter
@Setter
public abstract class Metrics {

    /**
     * Ip address of the local agent.
     */
    private InetAddress inetAddress;

    /**
     * Time stamp at the time of collection.
     */
    private Date date;
}
