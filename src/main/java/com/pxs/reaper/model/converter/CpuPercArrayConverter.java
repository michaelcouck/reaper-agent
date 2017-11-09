package com.pxs.reaper.model.converter;

import org.hyperic.sigar.CpuPerc;

public class CpuPercArrayConverter extends GenericConverter<CpuPerc[]> {

    CpuPercArrayConverter() {
        this.type = CpuPerc[].class;
    }

}