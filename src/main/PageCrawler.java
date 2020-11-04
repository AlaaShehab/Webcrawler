package main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageCrawler implements Runnable{
    private WebCrawler crawler;
    private String page;

    public PageCrawler (WebCrawler crawler, String page) {
        this.crawler = crawler;
        this.page = page;
    }

    @Override
    public void run() {
        try {
            // Add page to processed list.
            crawler.addProcessedPage(page);

            List<String> links = new ArrayList<>();
            // Connect to the website.
            Document document = Jsoup.connect(page).get();
            // Parse the html document and extract all links.
            Elements linksOnPage = document.select("a[href]");

            // Loop on all links found on the page.
            for (Element page : linksOnPage) {
                String link = page.attr("abs:href");
                // Add a new page to the list only if it has same domain
                // as starting site and if not visited before.
                if (sameDomain(link) && !crawler.isPageVisited(link)) {
                    links.add(link);
                    crawler.addUnprocessedPage(link);
                }
            }
            crawler.writePagesToFile(page, links);
        } catch (IOException | InterruptedException e) {
            System.err.println("For '" + page + "': " + e.getMessage());
        }
    }

    private boolean sameDomain(String pageLink) {
        return pageLink.matches(crawler.getDomain() + "[\\w\\d|/]*");
    }
}
