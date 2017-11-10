package trash.converter;

import com.pxs.reaper.Constant;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
@SuppressWarnings("WeakerAccess")
public abstract class GenericConverter<E> implements AttributeConverter<E, String> {

    protected Class<E> type;

    @Override
    public String convertToDatabaseColumn(final E e) {
        return Constant.GSON.toJson(e);
    }

    @Override
    public E convertToEntityAttribute(final String json) {
        return Constant.GSON.fromJson(json, type);
    }
}