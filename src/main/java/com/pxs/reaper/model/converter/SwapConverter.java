package com.pxs.reaper.model.converter;

import org.hyperic.sigar.Swap;

public class SwapConverter extends GenericConverter<Swap> {

    SwapConverter() {
        this.type = Swap.class;
    }

}