package trash.converter;

import org.hyperic.sigar.Swap;

import javax.persistence.Converter;

@Converter
public class SwapConverter extends GenericConverter<Swap> {

    public SwapConverter() {
        this.type = Swap.class;
    }

}