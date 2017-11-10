package trash.converter;

import javax.persistence.Converter;
import java.lang.management.MemoryUsage;

@Converter
public class MemoryUsageConverter extends GenericConverter<MemoryUsage> {

    public MemoryUsageConverter() {
        this.type = MemoryUsage.class;
    }

}