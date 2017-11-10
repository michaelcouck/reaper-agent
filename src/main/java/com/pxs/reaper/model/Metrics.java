package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Timestamp;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Metrics {

    protected long id;

    /**
     * Ip address of the local agent.
     */
    private String ipAddress;

    /**
     * Time stamp at the time of collection.
     */
    private Date date;

    private Timestamp created;
    private Timestamp updated;

}
