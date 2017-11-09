package com.pxs.reaper.model.converter;

import org.hyperic.sigar.Mem;

public class MemConverter extends GenericConverter<Mem> {

    MemConverter() {
        this.type = Mem.class;
    }

}