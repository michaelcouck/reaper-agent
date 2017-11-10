package trash;

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
public class OSMetricsTimestampListener {

    @PrePersist
    public void prePersist(final OSMetrics persistable) {
        persistable.setCreated(new Timestamp(System.currentTimeMillis()));
        persistable.setUpdated(new Timestamp(System.currentTimeMillis()));
    }

    @PreUpdate
    public void preUpdate(final OSMetrics persistable) {
        persistable.setUpdated(new Timestamp(System.currentTimeMillis()));
    }

}
