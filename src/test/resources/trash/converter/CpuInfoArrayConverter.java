package trash.converter;

import org.hyperic.sigar.CpuInfo;

import javax.persistence.Converter;

@Converter
public class CpuInfoArrayConverter extends GenericConverter<CpuInfo[]> {

    public CpuInfoArrayConverter() {
        this.type = CpuInfo[].class;
    }

}