package com.pxs.reaper.model;

// import com.couchbase.client.java.repository.annotation.Field;
// import com.couchbase.client.java.repository.annotation.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.annotation.Id;
// import org.springframework.data.couchbase.core.mapping.Document;

@Getter
@Setter
@ToString
// @Document
@SuppressWarnings("WeakerAccess")
public abstract class Metrics {

    public Metrics() {
        setCreated(System.currentTimeMillis());
        setCodeBase(FilenameUtils.getName(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()));
    }

    // @Id
    @Id
    protected String id;

    // @Field
    private String codeBase;

    // @Field
    private String type = this.getClass().getName();

    /**
     * Ip address of the local agent.
     */
    // @Field
    private String ipAddress;

    // @Field
    private long created;

}
