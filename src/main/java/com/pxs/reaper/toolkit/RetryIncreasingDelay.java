package com.pxs.reaper.toolkit;

import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class takes a function and retries a specified number of times with an increasing delay between retries.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 01-11-2017
 */
public class RetryIncreasingDelay implements Retry {

    private Logger log = Logger.getLogger(this.getClass().getSimpleName());

    /**
     * {@inheritDoc}
     */
    public <I, O> O retry(final Function<I, O> functionToRetry, final I input, final int numberOfRetries, final long finalDelayBetweenRetries) {
        return retryWithIncreasingDelay(functionToRetry, input, numberOfRetries, finalDelayBetweenRetries);
    }

    /**
     * See: {@link RetryIncreasingDelay#retry(Function, Object, int, long)}
     */
    private <I, O> O retryWithIncreasingDelay(
            final Function<I, O> functionToRetry,
            final I input,
            final int numberOfRetries,
            final long finalDelayBetweenRetries) {
        try {
            return functionToRetry.apply(input);
        } catch (final Exception e) {
            if (numberOfRetries > 0) {
                long time = finalDelayBetweenRetries / Math.max(1, numberOfRetries);
                sleep(time);
                return retryWithIncreasingDelay(functionToRetry, input, numberOfRetries - 1, finalDelayBetweenRetries);
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private void sleep(final long time) {
        try {
            log.log(Level.INFO, "Sleeping for : " + time);
            Thread.sleep(time);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}