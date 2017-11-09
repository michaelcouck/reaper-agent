package com.pxs.reaper.model.converter;

import org.hyperic.sigar.ProcStat;

public class ProcStatConverter extends GenericConverter<ProcStat> {

    ProcStatConverter() {
        this.type = ProcStat.class;
    }

}