package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class WebCrawler {
    static final String NO_PAGE_FOUND = "No Page Found";
    private static final int SEMAPHORE_PERMITS_NUM = 1;

    private String domain;
    private Set<String> unprocessedPages;
    private Set<String> processedPages;
    private FileWriter fileWriter;

    private Semaphore fileLock;
    private Semaphore processedPagesLock;
    private Semaphore unprocessedPagesLock;

    public WebCrawler (String domain, String filename) throws IOException {
        this.domain = domain;
        unprocessedPages = Collections.synchronizedSet(new HashSet<>());
        processedPages = Collections.synchronizedSet(new HashSet<>());
        fileWriter = new FileWriter(new File(filename), true);

        fileLock = new Semaphore(SEMAPHORE_PERMITS_NUM);
        processedPagesLock = new Semaphore(SEMAPHORE_PERMITS_NUM);
        unprocessedPagesLock = new Semaphore(SEMAPHORE_PERMITS_NUM);

        unprocessedPages.add(domain);
    }

    // Add a new unprocessed page to the unprocessed pages list.
    void addUnprocessedPage(String page) throws InterruptedException {
        unprocessedPagesLock.acquire();
        unprocessedPages.add(page);
        unprocessedPagesLock.release();
    }

    // Add the processed page to the processed pages list for further
    // reference.
    void addProcessedPage(String page) throws InterruptedException {
        processedPagesLock.acquire();
        processedPages.add(page);
        processedPagesLock.release();
    }

    // Checks if the page is visited before i.e. has already been processed
    // or has been added but not processed yet.
    boolean isPageVisited(String page) throws InterruptedException {
        processedPagesLock.acquire();
        unprocessedPagesLock.acquire();

        boolean isPageVisited = processedPages.contains(page)
                                    || unprocessedPages.contains(page);

        processedPagesLock.release();
        unprocessedPagesLock.release();
        return isPageVisited;
    }

    // Returns the starting page link.
    String getDomain() {
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

    public int unprocessedPagesSize () throws InterruptedException {
        unprocessedPagesLock.acquire();
        int size = unprocessedPages.size();
        unprocessedPagesLock.release();
        return size;
    }

    // Write all visited page with the links found in them.
    void writePagesToFile(String page, List<String> links)
            throws InterruptedException, IOException {
        fileLock.acquire();
        fileWriter.write("Page : " + page + "\n");
        for (String link : links) {
            fileWriter.write(link + "\n");
        }
        fileLock.release();
    }

    void closeFile() throws IOException {
        fileWriter.close();
    }
}
