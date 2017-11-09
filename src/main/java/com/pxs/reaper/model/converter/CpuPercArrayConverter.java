package com.pxs.reaper.model.converter;

import org.hyperic.sigar.CpuPerc;

import javax.persistence.Converter;

@Converter
public class CpuPercArrayConverter extends GenericConverter<CpuPerc[]> {

    CpuPercArrayConverter() {
        this.type = CpuPerc[].class;
    }

}