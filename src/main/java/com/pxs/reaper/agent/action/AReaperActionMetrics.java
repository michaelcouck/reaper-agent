package com.pxs.reaper.agent.action;

import com.pxs.reaper.agent.action.instrumentation.NetworkTrafficCollector;
import com.pxs.reaper.agent.model.Metrics;
import com.pxs.reaper.agent.model.NetworkNode;

import java.util.TreeSet;

/**
 * Common logic for all metrics gathering actions.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 14-04-2018
 */
public abstract class AReaperActionMetrics implements ReaperAction {

    /**
     * Gets the network through put from the {@link NetworkTrafficCollector}, adds the nodes
     * to the metrics object, and clears the network through put map of nodes for the next iteration.
     *
     * @param metrics the metrics to post to the analysis service
     */
    void networkThroughput(final Metrics metrics) {
        synchronized (NetworkTrafficCollector.NETWORK_NODE) {
            NetworkNode networkNode = new NetworkNode();
            networkNode.setLocalAddress(NetworkTrafficCollector.NETWORK_NODE.getLocalAddress());
            networkNode.setAddressPortThroughPut(new TreeSet<>(NetworkTrafficCollector.NETWORK_NODE.getAddressPortThroughPut()));
            metrics.setNetworkNode(networkNode);
            NetworkTrafficCollector.NETWORK_NODE.getAddressPortThroughPut().clear();
        }
    }

}