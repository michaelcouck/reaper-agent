@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(value = DateFormatterAdapter.class, type = Date.class),
        @XmlJavaTypeAdapter(value = TimestampFormatterAdapter.class, type = Timestamp.class)
})
package com.pxs.reaper.model;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.sql.Timestamp;
import java.util.Date;
