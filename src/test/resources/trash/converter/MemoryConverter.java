package trash.converter;

import com.pxs.reaper.model.Memory;

import javax.persistence.Converter;

@Converter
public class MemoryConverter extends GenericConverter<Memory> {

    public MemoryConverter() {
        this.type = Memory.class;
    }

}