
package com.pxs.reaper.model.converter;

import org.hyperic.sigar.NetInfo;

import javax.persistence.Converter;

@Converter
public class NetInfoConverter extends GenericConverter<NetInfo> {

    NetInfoConverter() {
        this.type = NetInfo.class;
    }

}