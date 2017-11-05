package com.pxs.reaper.transport;

/**
 * Interface for a variety of TRANSPORT implementations, could be web sockets, or some or other publish subscribe
 * mechanism, like Kafka or Google Pub/Sub.
 *
 * @author Michael Couck
 * @version 1.0
 * @since 20-10-2017
 */
public interface Transport {

    /**
     * Delivers the object to the endpoint for TRANSPORT over the wire. Typically implementations will convert
     * the objects to Json for TRANSPORT, but could be another protocol.
     *
     * @param metrics the metrics/telemetry data for a particular process, could be the operating system, but could
     *                be a Java process
     */
    void postMetrics(final Object metrics);

}
