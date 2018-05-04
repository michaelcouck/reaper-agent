package com.pxs.reaper.agent.toolkit;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class PassionPointsCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(zip|gz))$");

    @Override
    public boolean shouldVisit(final Page referringPage, final WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith("https://www.proximus.be/music");
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(final Page page) {
        String url = page.getWebURL().getURL();
        byte[] content = page.getContentData();
        System.out.println("URL: " + url + ", length : " +content.length);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            System.out.println("Text length: " + text.length());
            System.out.println("Html length: " + html.length());
            System.out.println("Outgoing links: " + links.size());
        }
    }

}
