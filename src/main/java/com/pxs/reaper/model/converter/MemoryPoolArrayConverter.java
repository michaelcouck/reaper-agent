package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.MemoryPool;

import javax.persistence.Converter;

@Converter
public class MemoryPoolArrayConverter extends GenericConverter<MemoryPool[]> {

    MemoryPoolArrayConverter() {
        this.type = MemoryPool[].class;
    }

}