package trash.converter;

import org.hyperic.sigar.ProcStat;

import javax.persistence.Converter;

@Converter
public class ProcStatConverter extends GenericConverter<ProcStat> {

    public ProcStatConverter() {
        this.type = ProcStat.class;
    }

}