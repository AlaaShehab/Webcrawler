import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Semaphore;

public class WebCrawler {
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

    public void addUnprocessedPage (String page) throws InterruptedException {
        unprocessedPagesLock.acquire();
        unprocessedPages.add(page);
        unprocessedPagesLock.release();
    }

    public void addProcessedPage (String page) throws InterruptedException {
        processedPagesLock.acquire();
        processedPages.add(page);
        processedPagesLock.release();
    }

    public boolean isPageVisited (String page) throws InterruptedException {
        processedPagesLock.acquire();
        unprocessedPagesLock.acquire();

        boolean isPageVisited = processedPages.contains(page) || unprocessedPages.contains(page);

        processedPagesLock.release();
        unprocessedPagesLock.release();
        return isPageVisited;
    }

    public String getDomain () {
        return domain;
    }

    public String getNextUnprocessedPage () throws InterruptedException {
        unprocessedPagesLock.acquire();
        String page = unprocessedPages.iterator().hasNext()
                ? unprocessedPages.iterator().next()
                : "No page found";
        unprocessedPagesLock.release();
        return page;
    }

    public void removeUnprocessedPage (String page) throws InterruptedException {
        unprocessedPagesLock.acquire();
        unprocessedPages.remove(page);
        unprocessedPagesLock.release();
    }

    public boolean isUnprocessedPagesEmpty () throws InterruptedException {
        unprocessedPagesLock.acquire();
        boolean isEmpty = unprocessedPages.size() == 0;
        unprocessedPagesLock.release();
        return isEmpty;
    }

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
