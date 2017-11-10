package trash.converter;

import com.pxs.reaper.model.GarbageCollection;

import javax.persistence.Converter;

@Converter
public class GarbageCollectionArrayConverter extends GenericConverter<GarbageCollection[]> {

    public GarbageCollectionArrayConverter() {
        this.type = GarbageCollection[].class;
    }

}