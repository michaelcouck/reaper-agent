package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.Threading;

import javax.persistence.Converter;

@Converter
public class ThreadingConverter extends GenericConverter<Threading> {

    ThreadingConverter() {
        this.type = Threading.class;
    }

}