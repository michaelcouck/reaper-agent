package trash.converter;

import org.hyperic.sigar.Cpu;

import javax.persistence.Converter;

@Converter()
public class CpuArrayConverter extends GenericConverter<Cpu[]> {

    public CpuArrayConverter() {
        this.type = Cpu[].class;
    }

}