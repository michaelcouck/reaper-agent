package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.GarbageCollection;

public class GarbageCollectionArrayConverter extends GenericConverter<GarbageCollection[]> {

    GarbageCollectionArrayConverter() {
        this.type = GarbageCollection[].class;
    }

}