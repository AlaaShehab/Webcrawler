import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunnerTool {
    private static final int MAX_TIME_OUT = 60000;

    public static void main(String[] args) throws IOException, InterruptedException {
        WebCrawler crawler = new WebCrawler("https://monzo.com/", "Output.txt");
        ExecutorService pool = Executors.newCachedThreadPool();

        long startTime = System.currentTimeMillis();

        while (!isPageReadTimedOut(startTime)) {
            if (!crawler.isUnprocessedPagesEmpty()) {
                pool.execute(new PageCrawler(crawler, crawler.getNextUnprocessedPage()));
                startTime = System.currentTimeMillis();
            }
        }
        pool.shutdown();
        crawler.closeFile();
    }

    private static boolean isPageReadTimedOut(Long startTime) {
        return System.currentTimeMillis() - startTime > MAX_TIME_OUT;
    }
}
