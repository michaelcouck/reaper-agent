package com.pxs.reaper.model.converter;

import org.hyperic.sigar.Tcp;

public class TcpConverter extends GenericConverter<Tcp> {

    TcpConverter() {
        this.type = Tcp.class;
    }

}