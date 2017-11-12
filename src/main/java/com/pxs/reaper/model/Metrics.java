package com.pxs.reaper.model;

import com.couchbase.client.java.repository.annotation.Field;
import com.couchbase.client.java.repository.annotation.Id;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.couchbase.core.mapping.Document;

@Getter
@Setter
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("WeakerAccess")
public abstract class Metrics {

    @Id
    protected String id;

    @Field
    private String type = this.getClass().getName();

    /**
     * Ip address of the local agent.
     */
    @Field
    private String ipAddress;

    @Field
    private long created;

}
