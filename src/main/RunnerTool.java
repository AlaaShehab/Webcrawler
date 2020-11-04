package main;

import main.PageCrawler;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunnerTool {
    private static final int MAX_TIME_OUT_MILLIS = 100000;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Initialize the web crawler with site starting page and output filename.
        WebCrawler crawler = new WebCrawler("https://monzo.com/", "Output.txt");
        // Initialize the thread pool.
        ExecutorService pool = Executors.newCachedThreadPool();

        long startTime = System.currentTimeMillis();

        // Loop until no new pages are added for MAX_TIME_OUT_MILLIS period.
        while (!isPageReadTimedOut(startTime)) {
            String nextPage = crawler.getNextUnprocessedPage();
            if (!nextPage.equals(WebCrawler.NO_PAGE_FOUND)) {
                // Find all links in the nextPage.
                pool.execute(new PageCrawler(crawler, nextPage));
                startTime = System.currentTimeMillis();
            }
        }
        pool.shutdown();
        crawler.closeFile();
    }

    // Returns true if no new page is added during the MAX_TIME_OUT_MILLIS period.
    private static boolean isPageReadTimedOut(Long startTime) {
        return System.currentTimeMillis() - startTime > MAX_TIME_OUT_MILLIS;
    }
}
