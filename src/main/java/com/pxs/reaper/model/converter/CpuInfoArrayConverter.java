package com.pxs.reaper.model.converter;

import org.hyperic.sigar.CpuInfo;

import javax.persistence.Converter;

@Converter
public class CpuInfoArrayConverter extends GenericConverter<CpuInfo[]> {

    CpuInfoArrayConverter() {
        this.type = CpuInfo[].class;
    }

}