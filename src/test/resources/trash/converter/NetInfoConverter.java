
package trash.converter;

import org.hyperic.sigar.NetInfo;

import javax.persistence.Converter;

@Converter
public class NetInfoConverter extends GenericConverter<NetInfo> {

    public NetInfoConverter() {
        this.type = NetInfo.class;
    }

}