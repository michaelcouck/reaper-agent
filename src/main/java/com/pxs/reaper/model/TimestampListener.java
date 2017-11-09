package com.pxs.reaper.model;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.sql.Timestamp;

/**
 * This listener will insert the timestamp when the entity gets persisted or updated.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 29-09-2012
 */
public class TimestampListener {

    @PrePersist
    public void prePersist(final Metrics persistable) {
        persistable.setCreated(new Timestamp(System.currentTimeMillis()));
        persistable.setUpdated(new Timestamp(System.currentTimeMillis()));
    }

    @PreUpdate
    public void preUpdate(final Metrics persistable) {
        persistable.setUpdated(new Timestamp(System.currentTimeMillis()));
    }

}
