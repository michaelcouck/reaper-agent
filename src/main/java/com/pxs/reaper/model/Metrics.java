package com.pxs.reaper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter

@Entity
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(value = {TimestampListener.class})
public abstract class Metrics {

    /**
     * Ip address of the local agent.
     */
    @Column
    private String ipAddress;

    /**
     * Time stamp at the time of collection.
     */
    @Column
    private Date date;

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PERSISTABLE")
    @SequenceGenerator(name = "PERSISTABLE", sequenceName = "PERSISTABLE", allocationSize = 1000)
    protected long id;

    @Column
    private Timestamp created;
    @Column
    private Timestamp updated;
}
