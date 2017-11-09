
package com.pxs.reaper.model.converter;

import org.hyperic.sigar.NetConnection;

public class NetConnectionArrayConverter extends GenericConverter<NetConnection[]> {

    NetConnectionArrayConverter() {
        this.type = NetConnection[].class;
    }

}