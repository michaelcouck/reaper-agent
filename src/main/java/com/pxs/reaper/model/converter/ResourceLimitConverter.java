package com.pxs.reaper.model.converter;

import org.hyperic.sigar.ResourceLimit;

import javax.persistence.Converter;

@Converter
public class ResourceLimitConverter extends GenericConverter<ResourceLimit> {

    ResourceLimitConverter() {
        this.type = ResourceLimit.class;
    }

}