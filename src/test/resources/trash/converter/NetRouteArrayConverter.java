
package trash.converter;

import org.hyperic.sigar.NetRoute;

import javax.persistence.Converter;

@Converter
public class NetRouteArrayConverter extends GenericConverter<NetRoute[]> {

    public NetRouteArrayConverter() {
        this.type = NetRoute[].class;
    }

}