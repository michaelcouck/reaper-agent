package trash.converter;

import com.pxs.reaper.model.MemoryPool;

import javax.persistence.Converter;

@Converter
public class MemoryPoolArrayConverter extends GenericConverter<MemoryPool[]> {

    public MemoryPoolArrayConverter() {
        this.type = MemoryPool[].class;
    }

}