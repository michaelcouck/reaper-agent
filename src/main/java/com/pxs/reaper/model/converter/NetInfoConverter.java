
package com.pxs.reaper.model.converter;

import org.hyperic.sigar.NetInfo;

public class NetInfoConverter extends GenericConverter<NetInfo> {

    NetInfoConverter() {
        this.type = NetInfo.class;
    }

}