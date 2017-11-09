package com.pxs.reaper.model.converter;

import org.hyperic.sigar.Cpu;

import javax.persistence.Converter;

@Converter()
public class CpuArrayConverter extends GenericConverter<Cpu[]> {

    CpuArrayConverter() {
        this.type = Cpu[].class;
    }

}