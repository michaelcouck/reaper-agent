package com.pxs.reaper.toolkit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class FILETest {

    @Test
    public void findFileRecursively() {
        String fileName = "reaper-agent-";
        File file = FILE.findFileRecursively(new File("."), fileName);
        // File file = FILE.findFileRecursively(new File("."), "libsigar-amd64-freebsd-6.so");
        Assert.assertNotNull(file);
        Assert.assertTrue(file.getName().contains(fileName));
    }

}
