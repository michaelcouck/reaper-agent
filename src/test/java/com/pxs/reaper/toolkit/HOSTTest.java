package com.pxs.reaper.toolkit;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class HOSTTest {

    @Test
    public void getHostname() throws Exception {
        String hostName = HOST.hostname();
        Assert.assertNotNull(hostName);
        Assert.assertFalse(hostName.startsWith("127"));
    }

}
