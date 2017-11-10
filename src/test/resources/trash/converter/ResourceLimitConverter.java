package trash.converter;

import org.hyperic.sigar.ResourceLimit;

import javax.persistence.Converter;

@Converter
public class ResourceLimitConverter extends GenericConverter<ResourceLimit> {

    public ResourceLimitConverter() {
        this.type = ResourceLimit.class;
    }

}