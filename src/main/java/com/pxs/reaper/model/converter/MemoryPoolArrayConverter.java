package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.MemoryPool;

public class MemoryPoolArrayConverter extends GenericConverter<MemoryPool[]> {

    MemoryPoolArrayConverter() {
        this.type = MemoryPool[].class;
    }

}