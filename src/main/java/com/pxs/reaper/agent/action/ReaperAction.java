package com.pxs.reaper.agent.action;

/**
 * Tagging interface for actions.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
interface ReaperAction extends Action {

    /**
     * Releases any resources, operating system agents, Java agents etc.
     *
     * @return whether the unit of work was successful. If resource release fails for any reason, the result should be false
     */
    boolean terminate();

}