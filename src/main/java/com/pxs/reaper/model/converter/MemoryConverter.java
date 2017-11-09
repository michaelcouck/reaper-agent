package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.Memory;

import javax.persistence.Converter;

@Converter
public class MemoryConverter extends GenericConverter<Memory> {

    MemoryConverter() {
        this.type = Memory.class;
    }

}