package com.pxs.reaper.action;

/**
 * Tagging interface for actions.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
interface ReaperAction {

    /**
     * Releases any resources, operating system agents, Java agents etc.
     */
    boolean terminate();

}
