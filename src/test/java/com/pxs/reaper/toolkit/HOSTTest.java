package com.pxs.reaper.toolkit;

import org.junit.Assert;
import org.junit.Test;

public class HOSTTest {

    @Test
    public void getHostname() throws Exception {
        String hostName = HOST.hostname();
        Assert.assertNotNull(hostName);
        Assert.assertFalse(hostName.startsWith("127"));
    }

}
