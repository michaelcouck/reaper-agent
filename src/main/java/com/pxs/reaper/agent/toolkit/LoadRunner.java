package com.pxs.reaper.agent.toolkit;

/**
 * This class is a load test class to load and stress test the passion points site.
 * <p>
 * Interesting java crawler : https://github.com/yasserg/crawler4j
 *
 * @author Michael Couck
 * @version 01.00
 * @since 02-05-2018
 */
public class LoadRunner {

    /**
     * <pre>
     * Load(linear): Start several crawlers, increasing the threads gradually until the errors start showing up
     * Stress(not linear) : Start several thread concurrently until errors start showing up
     * Endurance(linear): Keep a steady load on the servers for long time, changing the users and interaction but
     *      staying within the error margins for the application
     * </pre>
     */
    public static void main(final String[] args) {
    }

}