package com.pxs.reaper.model;

import com.couchbase.client.java.repository.annotation.Field;
import com.couchbase.client.java.repository.annotation.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.couchbase.core.mapping.Document;

@Getter
@Setter
@ToString
@Document
@SuppressWarnings("WeakerAccess")
public abstract class Metrics {

    public Metrics() {
        setCreated(System.currentTimeMillis());
    }

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
