package trash.converter;

import org.hyperic.sigar.Mem;

import javax.persistence.Converter;

@Converter
public class MemConverter extends GenericConverter<Mem> {

    public MemConverter() {
        this.type = Mem.class;
    }

}