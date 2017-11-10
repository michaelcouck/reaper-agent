package trash.converter;

import javax.persistence.Converter;

@Converter
public class DoubleArrayConverter extends GenericConverter<double[]> {

    public DoubleArrayConverter() {
        this.type = double[].class;
    }

}