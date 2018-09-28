package com.pxs.reaper.agent.runner;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class PassionPointsCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(zip|gz))$");

    public static final Map<String, Thruple> fetchedPages = new HashMap<>();

    @Override
    protected WebURL handleUrlBeforeProcess(final WebURL curURL) {
        String page = curURL.getURL();
        startFetch(page);
        return super.handleUrlBeforeProcess(curURL);
    }

    private void startFetch(final String page) {
        Thruple thruple = fetchedPages.get(page);
        if (thruple == null) {
            thruple = new Thruple();
            thruple.page = page;
            fetchedPages.put(page, thruple);
        }
        thruple.start = System.currentTimeMillis();
    }

    private void endFetch(final String page) {
        Thruple thruple = fetchedPages.get(page);
        thruple.fetches++;
        thruple.fetchTime += System.currentTimeMillis() - thruple.start;
        thruple.start = 0;
    }

    private void addError(final String page) {
        Thruple thruple = fetchedPages.get(page);
        thruple.errors = thruple.errors + 1;
    }

    @Override
    protected void onContentFetchError(final Page fetchedPage) {
        String page = fetchedPage.getWebURL().getURL();
        addError(page);
        endFetch(page);
        super.onContentFetchError(fetchedPage);
    }

    @Override
    protected void onUnhandledException(final WebURL webUrl, final Throwable e) {
        String page = webUrl.getURL();
        addError(page);
        endFetch(page);
        super.onUnhandledException(webUrl, e);
    }

    @Override
    protected void onParseError(final WebURL webUrl) {
        String page = webUrl.getURL();
        addError(page);
        endFetch(page);
        super.onParseError(webUrl);
    }

    @Override
    public boolean shouldVisit(final Page referringPage, final WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                && href.startsWith("https://www.proximus.be/music");
    }

    @Override
    public void visit(final Page page) {
        endFetch(page.getWebURL().getURL());

        String url = page.getWebURL().getURL();
        byte[] content = page.getContentData();
        System.out.println("URL : " + url + ", length : " + content.length);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            System.out.println("Text length : " + text.length() + ", html length : " + html.length() + ", links : " + links.size());
        }

    }

}