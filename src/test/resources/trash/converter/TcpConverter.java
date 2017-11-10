package trash.converter;

import org.hyperic.sigar.Tcp;

import javax.persistence.Converter;

@Converter
public class TcpConverter extends GenericConverter<Tcp> {

    public TcpConverter() {
        this.type = Tcp.class;
    }

}