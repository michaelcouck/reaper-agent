
package trash.converter;

import org.hyperic.sigar.NetConnection;

import javax.persistence.Converter;

@Converter
public class NetConnectionArrayConverter extends GenericConverter<NetConnection[]> {

    public NetConnectionArrayConverter() {
        this.type = NetConnection[].class;
    }

}