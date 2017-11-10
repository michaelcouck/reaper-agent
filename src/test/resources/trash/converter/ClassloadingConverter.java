package trash.converter;

import com.pxs.reaper.model.Classloading;

import javax.persistence.Converter;

@Converter
public class ClassloadingConverter extends GenericConverter<Classloading> {

    public ClassloadingConverter() {
        this.type = Classloading.class;
    }

}