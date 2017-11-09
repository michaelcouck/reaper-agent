package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.Classloading;

import javax.persistence.Converter;

@Converter
public class ClassloadingConverter extends GenericConverter<Classloading> {

    ClassloadingConverter() {
        this.type = Classloading.class;
    }

}