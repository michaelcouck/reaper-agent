package com.pxs.reaper.model.converter;

import org.hyperic.sigar.Mem;

import javax.persistence.Converter;

@Converter
public class MemConverter extends GenericConverter<Mem> {

    MemConverter() {
        this.type = Mem.class;
    }

}