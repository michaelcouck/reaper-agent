package com.pxs.reaper.model;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.sql.Timestamp;
import java.util.Date;

public class TimestampAdapter extends XmlAdapter<Date, Timestamp> {

    public Date marshal(final Timestamp v) {
        return new Date(v.getTime());
    }

    public Timestamp unmarshal(final Date v) {
        return new Timestamp(v.getTime());
    }

}
