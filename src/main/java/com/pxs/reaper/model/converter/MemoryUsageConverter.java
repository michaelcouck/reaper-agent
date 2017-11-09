package com.pxs.reaper.model.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.lang.management.MemoryUsage;

@Converter
public class MemoryUsageConverter implements AttributeConverter<MemoryUsage, String> {

    @Override
    public String convertToDatabaseColumn(final MemoryUsage attribute) {
        return null;
    }

    @Override
    public MemoryUsage convertToEntityAttribute(final String dbData) {
        return null;
    }

}