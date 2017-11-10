
package trash.converter;

import org.hyperic.sigar.NetStat;

import javax.persistence.Converter;

@Converter
public class NetStatConverter extends GenericConverter<NetStat> {

    public NetStatConverter() {
        this.type = NetStat.class;
    }

}