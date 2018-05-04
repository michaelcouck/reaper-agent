package com.pxs.reaper.agent.toolkit;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

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
        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }).start();

        String crawlStorageFolder = "/tmp/crawl/root";
        int numberOfCrawlers = 7;

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
        controller.start(PassionPointsCrawler.class, numberOfCrawlers);
    }

}