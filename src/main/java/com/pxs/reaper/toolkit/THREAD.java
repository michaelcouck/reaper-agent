package com.pxs.reaper.toolkit;

/**
 * This class just has a method that will wait for a list of threads to finish and
 * an executor service that will execute 'threads' and return futures that can be waited for
 * by the callers.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 12-02-2011
 */

public class THREAD {

    /**
     * This method will just sleep for the specified time without the interrupted exception needing to be caught.
     *
     * @param milliSeconds the time for the current thread to sleep in milli seconds
     */
    public static void sleep(final long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}
