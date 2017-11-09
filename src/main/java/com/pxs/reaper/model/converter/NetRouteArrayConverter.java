
package com.pxs.reaper.model.converter;

import org.hyperic.sigar.NetRoute;

public class NetRouteArrayConverter extends GenericConverter<NetRoute[]> {

    NetRouteArrayConverter() {
        this.type = NetRoute[].class;
    }

}