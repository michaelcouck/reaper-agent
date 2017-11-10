package trash.converter;

import org.hyperic.sigar.CpuPerc;

import javax.persistence.Converter;

@Converter
public class CpuPercArrayConverter extends GenericConverter<CpuPerc[]> {

    public CpuPercArrayConverter() {
        this.type = CpuPerc[].class;
    }

}