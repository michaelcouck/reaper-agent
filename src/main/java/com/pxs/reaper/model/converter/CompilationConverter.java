package com.pxs.reaper.model.converter;

import com.pxs.reaper.model.Compilation;

import javax.persistence.Converter;

@Converter
public class CompilationConverter extends GenericConverter<Compilation> {

    CompilationConverter() {
        this.type = Compilation.class;
    }

}