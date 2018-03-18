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
        String fileName = "libsigar-amd64-linux.so";
        File file = FILE.findFileRecursively(new File("."), fileName);
        Assert.assertNotNull(file);
        Assert.assertTrue(file.getName().contains(fileName));
    }

}
