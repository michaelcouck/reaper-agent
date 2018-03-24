package com.pxs.reaper.model;

import lombok.Getter;
import lombok.Setter;

/**
 * This class is transport for messaging between the micro service and the agents.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 23-03-2018
 */
@Setter
@Getter
public class Message {

    /**
     * Flag for the agent to start the scheduled jobs, i.e. collecting the metrics
     */
    private boolean start;
    /**
     * Flag for the agent to terminate any jobs that are scheduled on the host JVM
     */
    private boolean terminate;

}