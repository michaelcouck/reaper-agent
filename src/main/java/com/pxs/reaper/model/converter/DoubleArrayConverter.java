package com.pxs.reaper.model.converter;

import javax.persistence.Converter;

@Converter
public class DoubleArrayConverter extends GenericConverter<double[]> {

    DoubleArrayConverter() {
        this.type = double[].class;
    }

}