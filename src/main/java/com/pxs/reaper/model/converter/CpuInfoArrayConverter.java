package com.pxs.reaper.model.converter;

import org.hyperic.sigar.CpuInfo;

public class CpuInfoArrayConverter extends GenericConverter<CpuInfo[]> {

    CpuInfoArrayConverter() {
        this.type = CpuInfo[].class;
    }

}