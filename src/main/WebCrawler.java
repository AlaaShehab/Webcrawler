package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class WebCrawler {
    static final String NO_PAGE_FOUND = "No Page Found";
    private String domain;
    private Set<String> unprocessedPages;
    private Set<String> processedPages;
    private FileWriter fileWriter;

    private Semaphore wrt;
    private Semaphore processedPagesLock;
    private Semaphore unprocessedPagesLock;

    public WebCrawler (String domain, String filename) throws IOException {
        this.domain = domain;
        unprocessedPages = Collections.synchronizedSet(new HashSet<>());
        processedPages = Collections.synchronizedSet(new HashSet<>());
        fileWriter = new FileWriter(new File(filename), true);

        wrt = new Semaphore(1);
        processedPagesLock = new Semaphore(1);
        unprocessedPagesLock = new Semaphore(1);

        unprocessedPages.add(domain);
    }

    // Add a new unprocessed page to the list.
    public void addUnprocessedPage (String page) throws InterruptedException {
        unprocessedPagesLock.acquire();
        unprocessedPages.add(page);
        unprocessedPagesLock.release();
    }

    // Add the processed page to the list for further reference.
    public void addProcessedPage (String page) throws InterruptedException {
        processedPagesLock.acquire();
        processedPages.add(page);
        processedPagesLock.release();
    }

    // Checks if the page is visited before i.e. has already been processed
    // or has been added but not processed yet.
    public boolean isPageVisited (String page) throws InterruptedException {
        processedPagesLock.acquire();
        unprocessedPagesLock.acquire();

        boolean isPageVisited = processedPages.contains(page) || unprocessedPages.contains(page);

        processedPagesLock.release();
        unprocessedPagesLock.release();
        return isPageVisited;
    }

    // Returns the starting page link.
    public String getDomain () {
        return domain;
    }

    // Returns the next unprocessed available page from the list, then
    // removes it.
    public String getNextUnprocessedPage () throws InterruptedException {
        unprocessedPagesLock.acquire();
        String page = unprocessedPages.iterator().hasNext()
                ? unprocessedPages.iterator().next()
                : NO_PAGE_FOUND;
        unprocessedPages.remove(page);
        unprocessedPagesLock.release();
        return page;
    }

    // Write all visited page with the links found in them.
    public void writePagesToFile (String page, List<String> links) throws InterruptedException, IOException {
        wrt.acquire();
        fileWriter.write("Page : " + page + "\n");
        for (String link : links) {
            fileWriter.write(link + "\n");
        }
        wrt.release();
    }

    public void closeFile () throws IOException {
        fileWriter.close();
    }
}
