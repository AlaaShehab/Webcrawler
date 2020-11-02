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

    PageCrawler (WebCrawler crawler, String page) {
        this.crawler = crawler;
        this.page = page;
    }

    @Override
    public void run() {
        try {
            crawler.removeUnprocessedPage(page);
            crawler.addProcessedPage(page);

            List<String> links = new ArrayList<>();

            Document document = Jsoup.connect(page).get();
            Elements linksOnPage = document.select("a[href]");

            for (Element page : linksOnPage) {
                String link = page.attr("abs:href");
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
