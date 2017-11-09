
package com.pxs.reaper.model.converter;

import org.hyperic.sigar.NetStat;

public class NetStatConverter extends GenericConverter<NetStat> {

    NetStatConverter() {
        this.type = NetStat.class;
    }

}