package com.pxs.reaper.agent.runner;

import com.google.common.util.concurrent.AtomicDouble;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.Arrays;
import java.util.Map;

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
    public static void main(final String[] args) throws Exception {
        final int runTime = 60000;
        final double numberOfCrawlers = 25;
        new Thread(() -> {
            try {
                Thread.sleep(runTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            printStatistics(numberOfCrawlers);
            System.exit(0);
        }).start();

        String crawlStorageFolder = "/tmp/crawl/root";

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed("https://www.proximus.be/music");

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(PassionPointsCrawler.class, (int) numberOfCrawlers);
    }

    private static void printStatistics(final double numberOfCrawlers) {
        Map<String, Thruple> fetchedPages = PassionPointsCrawler.fetchedPages;

        double totalPagesFetched = 0;
        long smallestFetchTime = Integer.MAX_VALUE;
        long greatestFetchTime = 0;

        double[] errors = new double[fetchedPages.size()];
        double[] fetchTimes = new double[fetchedPages.size()];

        int offset = 0;
        for (final Map.Entry<String, Thruple> thrupleEntry : fetchedPages.entrySet()) {
            Thruple thruple = thrupleEntry.getValue();

            totalPagesFetched += thruple.fetches;
            errors[offset] = thruple.errors;
            fetchTimes[offset] = thruple.fetchTime;

            int averageFetchTime = thruple.fetchTime / Math.max(1, thruple.fetches);
            smallestFetchTime = smallestFetchTime < averageFetchTime ? smallestFetchTime : averageFetchTime;
            greatestFetchTime = greatestFetchTime > averageFetchTime ? greatestFetchTime : averageFetchTime;

            offset++;
        }

        Mean mean = new Mean();
        StandardDeviation standardDeviation = new StandardDeviation();

        AtomicDouble totalFetchTime = new AtomicDouble();
        Arrays.stream(fetchTimes).forEach(totalFetchTime::addAndGet);

        System.out.println("\n");

        // Average fetches per second
        System.out.println("Pages fetched per user per minute : " + totalPagesFetched / numberOfCrawlers);
        System.out.println("Total pages fetched               : " + totalPagesFetched);
        System.out.println("Total fetch time                  : " + totalFetchTime.get() * 1000);
        System.out.println("Fetches per second                : " + (totalPagesFetched / totalFetchTime.get() * 1000));
        System.out.println("Smallest fetch time               : " + smallestFetchTime);
        System.out.println("Greatest fetch time               : " + greatestFetchTime);
        System.out.println("Average fetch time                : " + mean.evaluate(fetchTimes));
        System.out.println("Standard deviation for fetch time : " + standardDeviation.evaluate(fetchTimes));

        // Average errors per page
        System.out.println("Average errors per page           : " + mean.evaluate(errors));
        // Standard deviation for errors
        System.out.println("Standard deviation for errors     : " + standardDeviation.evaluate(errors));
    }

}