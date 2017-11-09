package com.pxs.reaper.model.converter;

import org.hyperic.sigar.ResourceLimit;

public class ResourceLimitConverter extends GenericConverter<ResourceLimit> {

    ResourceLimitConverter() {
        this.type = ResourceLimit.class;
    }

}