package com.pxs.reaper.model.converter;

import org.hyperic.sigar.ProcStat;

import javax.persistence.Converter;

@Converter
public class ProcStatConverter extends GenericConverter<ProcStat> {

    ProcStatConverter() {
        this.type = ProcStat.class;
    }

}