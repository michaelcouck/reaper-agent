package com.pxs.reaper.toolkit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Michael Couck
 * @version 01.01
 * @since 24-03-2018
 */
public class THREADTest {

    @After
    public void after() {
        THREAD.destroy();
        THREAD.initialize();
    }

    @Test
    public void scheduleCancelAndStart() {
        long sleep = 10;
        AtomicInteger atomicInteger = new AtomicInteger();
        THREAD.scheduleAtFixedRate(atomicInteger::incrementAndGet, 1, 1);
        THREAD.sleep(sleep);
        Assert.assertEquals(sleep, atomicInteger.get(), 5);

        THREAD.cancelScheduledFutures();
        long increments = atomicInteger.get();
        THREAD.sleep(sleep);
        Assert.assertEquals(increments, atomicInteger.get());

        THREAD.startScheduledFutures();
        THREAD.sleep(sleep);
        Assert.assertEquals(increments + sleep, atomicInteger.get(), 5);
    }

}