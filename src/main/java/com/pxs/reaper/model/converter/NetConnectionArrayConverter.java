
package com.pxs.reaper.model.converter;

import org.hyperic.sigar.NetConnection;

import javax.persistence.Converter;

@Converter
public class NetConnectionArrayConverter extends GenericConverter<NetConnection[]> {

    NetConnectionArrayConverter() {
        this.type = NetConnection[].class;
    }

}