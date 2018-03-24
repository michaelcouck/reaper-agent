package com.pxs.reaper.action;

/**
 * This is a tagging interface for scheduled actions. Initial implementation is simply a {@link java.util.Timer} task
 * that is scheduled to run periodically, however subsequent requirements dictate that the tasks can be cancelled, and then
 * restarted if necessary. This interface will be the adapter for the {@link com.pxs.reaper.toolkit.THREAD} API for
 * managing futures and scheduled tasks in the agent.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-03-2018
 */
public interface Action extends Runnable {

    void run();

}
