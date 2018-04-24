package com.pxs.reaper.agent.toolkit;

import com.pxs.reaper.agent.action.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class just has a method that will wait for a list of threads to finish and
 * an executor service that will execute 'threads' and return futures that can be waited for
 * by the callers.
 *
 * @author Michael Couck
 * @version 01.01
 * @since 24-03-2018
 */
@SuppressWarnings("WeakerAccess")
public class THREAD {

    /**
     * Saves a pointer to all the runnables that they can be restarted if paused.
     */
    private static Map<Action, Long> SCHEDULED_RUNNABLES;
    /**
     * Saves pointers to the individual futures to cancel them explicitly if needed.
     */
    private static Map<Action, ScheduledFuture> SCHEDULED_FUTURES;
    /**
     * Executes the 'threads' and returns a future.
     */
    private static ScheduledExecutorService EXECUTOR_SERVICE;

    static {
        initialize();
    }

    /**
     * This method initializes the executor service, and the thread pool that will execute runnables.
     */
    public static void initialize() {
        if (EXECUTOR_SERVICE != null && !EXECUTOR_SERVICE.isShutdown()) {
            System.out.println("Executor service already initialized : ");
            return;
        }

        EXECUTOR_SERVICE = Executors.newScheduledThreadPool(10);
        SCHEDULED_RUNNABLES = new HashMap<>();
        SCHEDULED_FUTURES = new HashMap<>();
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
    }

    public static void schedule(final Action action, final long initialDelay) {
        EXECUTOR_SERVICE.schedule(action, initialDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Starts a runnable on a scheduled execution time table.
     *
     * @param action       the action(must be a {@link Runnable}) to be scheduled periodically
     * @param initialDelay the initial delay to wait before running the action for the first time
     * @param period       the delay between runs of the action
     */
    public static void scheduleAtFixedRate(final Action action, final long initialDelay, final long period) {
        ScheduledFuture scheduledFuture = EXECUTOR_SERVICE.scheduleAtFixedRate(action, initialDelay, period, TimeUnit.MILLISECONDS);
        SCHEDULED_FUTURES.put(action, scheduledFuture);
        SCHEDULED_RUNNABLES.put(action, period);
    }

    /**
     * Starts all cancelled actions. This is a convenience method to pause and restart the actions. The
     * initial delay and the period for the restart are taken from the period rather than the initial delay
     * parameter from the {@link THREAD#scheduleAtFixedRate(Action, long, long)} method.
     */
    public static void startScheduledFutures() {
        for (final Action action : SCHEDULED_RUNNABLES.keySet()) {
            long initialDelay = SCHEDULED_RUNNABLES.get(action), period = SCHEDULED_RUNNABLES.get(action);
            scheduleAtFixedRate(action, initialDelay, period);
        }
    }

    /**
     * Cancels all the futures that have been scheduled. The runnable targets in the schedules are
     * retained however, and the period between runs, i.e. the scheduled period is retained, and can be
     * used to re-start the actions.
     */
    public static void cancelScheduledFutures() {
        for (final ScheduledFuture scheduledFuture : SCHEDULED_FUTURES.values()) {
            System.out.println("Terminating future : " + scheduledFuture);
            scheduledFuture.cancel(Boolean.TRUE);
        }
    }

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

    /**
     * Clears all the schedules, including the stored actions, and the shuts down the executor service. The
     * result of this action means that the {@link THREAD#initialize()} needs to be called again explicitly to
     * re-initialize the executor.
     */
    public static void destroy() {
        EXECUTOR_SERVICE.shutdown();
        SCHEDULED_FUTURES.clear();
        SCHEDULED_RUNNABLES.clear();
    }

    /**
     * Convenience thread to shut down the scheduled tasks elegantly, if that is at all possible.
     */
    private static class ShutdownThread extends Thread {
        public void run() {
            try {
                THREAD.destroy();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

}