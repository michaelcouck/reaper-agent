package com.pxs.reaper.toolkit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@RunWith(MockitoJUnitRunner.class)
public class RetryIncreasingDelayTest {

    @Spy
    private RetryIncreasingDelay retryIncreasingDelay;
    @Mock
    private Function<String, String> function;

    @Test
    public void retry() {
        //noinspection EmptyCatchBlock
        int maxRetries = 3;
        long timeToSleep = 3000L;
        AtomicInteger retries = new AtomicInteger();
        try {
            Mockito.when(function.apply(Mockito.anyString())).then(invocation -> {
                throw new RuntimeException("Retry : " + retries.incrementAndGet());
            });
            retryIncreasingDelay.retry(function, null, maxRetries, timeToSleep);
            Assert.fail("Should have thrown an exception on the 4 the try : ");
        } catch (final Exception e) {
            // Ignored, and expected
        }

        String input = "Hello world!";
        Mockito.doAnswer(invocation -> input).when(function).apply(Matchers.anyString());
        // Mockito.when(function.apply(Mockito.anyString())).then(invocation -> input);
        String output = retryIncreasingDelay.retry(function, null, maxRetries, timeToSleep);
        Assert.assertEquals(input, output);
    }

}
