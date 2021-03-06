package com.pxs.reaper.agent.runner;

import com.google.common.util.concurrent.AtomicDouble;
import com.pxs.reaper.agent.toolkit.FILE;
import com.pxs.reaper.agent.toolkit.THREAD;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.io.File;
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
@SuppressWarnings({"WeakerAccess", "DanglingJavadoc"})
public class LoadRunner {

    /**
     * Load(linear): Start several crawlers, increasing the threads gradually until the errors start showing up
     * Stress(not linear) : Start several thread concurrently until errors start showing up
     * Endurance(linear): Keep a steady load on the servers for long time, changing the users and interaction but
     * staying within the error margins for the application
     */
    public static void main(final String[] args) throws Exception {
        final int runTime = 60000;
        final double numberOfCrawlers = 25;
        new Thread(() -> {
            THREAD.sleep(runTime);
            printStatistics(numberOfCrawlers);
            System.exit(0);
        }).start();

        File file = new File("./tmp/crawl/root");
        if (!file.exists()) {
            boolean createdFolders = file.mkdirs();
            if (!createdFolders) {
                throw new RuntimeException("Couldn't create folders : " + file.getAbsolutePath());
            }
        }
        String crawlStorageFolder = FILE.cleanFilePath(file.getAbsolutePath());

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("https://www.proximus.be/music");

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
        System.out.println("Total fetch time                  : " + totalFetchTime.get() / 1000 + " (s)");
        System.out.println("Fetches per second                : " + totalPagesFetched / (totalFetchTime.get() / 1000));
        System.out.println("Smallest fetch time               : " + smallestFetchTime / 1000 + " (s)");
        System.out.println("Greatest fetch time               : " + greatestFetchTime / 1000 + " (s)");
        System.out.println("Average fetch time                : " + mean.evaluate(fetchTimes) / 1000 + " (s)");
        System.out.println("Standard deviation for fetch time : " + standardDeviation.evaluate(fetchTimes) / 1000 + " (s)");

        // Average errors per page
        System.out.println("Average errors per page           : " + mean.evaluate(errors));
        // Standard deviation for errors
        System.out.println("Standard deviation for errors     : " + standardDeviation.evaluate(errors));
    }

}