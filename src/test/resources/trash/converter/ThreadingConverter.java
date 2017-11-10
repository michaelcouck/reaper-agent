package trash.converter;

import com.pxs.reaper.model.Threading;

import javax.persistence.Converter;

@Converter
public class ThreadingConverter extends GenericConverter<Threading> {

    public ThreadingConverter() {
        this.type = Threading.class;
    }

}