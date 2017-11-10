package trash.converter;

import com.pxs.reaper.model.Compilation;

import javax.persistence.Converter;

@Converter
public class CompilationConverter extends GenericConverter<Compilation> {

    public CompilationConverter() {
        this.type = Compilation.class;
    }

}