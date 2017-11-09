package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.GarbageCollection;

import javax.persistence.Converter;

@Converter
public class GarbageCollectionArrayConverter extends GenericConverter<GarbageCollection[]> {

    GarbageCollectionArrayConverter() {
        this.type = GarbageCollection[].class;
    }

}