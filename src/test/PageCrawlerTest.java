package test;

import main.PageCrawler;
import main.WebCrawler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
public class PageCrawlerTest {
    private static final String SITE_URL = "site_url";
    private static final String FILE_NAME = "file_name";
    @Mock private Connection connection;
    private WebCrawler webCrawler;

    @Before
    public void setUp() throws IOException, InterruptedException {
        PowerMockito.mockStatic(Jsoup.class);
        connection = Mockito.mock(Connection.class);
        PowerMockito.when(Jsoup.connect(Mockito.anyString())).thenReturn(connection);
        webCrawler = new WebCrawler(SITE_URL, FILE_NAME);
        doAnswer(invocation -> null)
                .when(webCrawler).writePagesToFile(any(String.class), any(List.class));

    }

    @Test
    public void pageCrawler_noLinks_noPageAdded () throws IOException, InterruptedException {
        String page = buildSites(0, SITE_URL, 0);
        Document document = Jsoup.parse(page);
        PowerMockito.when(connection.get()).thenReturn(document);

        PageCrawler pageCrawler = new PageCrawler(webCrawler, SITE_URL);
        pageCrawler.run();

        verify(webCrawler, never()).addUnprocessedPage(any(String.class));
    }

    @Test
    public void pageCrawler_sameDomainLink_onePageAdded () throws IOException, InterruptedException {
        String page = buildSites(1, SITE_URL, 1);
        Document document = Jsoup.parse(page);
        PowerMockito.when(connection.get()).thenReturn(document);

        PageCrawler pageCrawler = new PageCrawler(webCrawler, SITE_URL);
        pageCrawler.run();

        verify(webCrawler, times(1)).addUnprocessedPage(any(String.class));
    }

    @Test
    public void pageCrawler_differentDomainLink_noPageAdded () throws IOException, InterruptedException {
        String page = buildSites(1, SITE_URL, 0);
        Document document = Jsoup.parse(page);
        PowerMockito.when(connection.get()).thenReturn(document);

        PageCrawler pageCrawler = new PageCrawler(webCrawler, SITE_URL);
        pageCrawler.run();

        verify(webCrawler, never()).addUnprocessedPage(any(String.class));
    }

    @Test
    public void pageCrawler_allLinksType_somePagesAdded () throws IOException, InterruptedException {
        String page = buildSites(5, SITE_URL, 3);
        Document document = Jsoup.parse(page);
        PowerMockito.when(connection.get()).thenReturn(document);

        PageCrawler pageCrawler = new PageCrawler(webCrawler, SITE_URL);
        pageCrawler.run();

        verify(webCrawler, times(3)).addUnprocessedPage(any(String.class));
    }

    private String buildSites(int numberOfLinks, String domain, int numberOfSameDomainLinks) {
        StringBuilder siteHTML = new StringBuilder("<body>");

        siteHTML.append("\n</body>");
        for (int i = 0; i < numberOfSameDomainLinks; i++) {
            siteHTML.append("\n\t<a href=\"");
            siteHTML.append(domain);
            siteHTML.append("/link");
            siteHTML.append(i);
            siteHTML.append("\"</a>");
        }

        for (int i = numberOfSameDomainLinks; i < numberOfLinks; i++) {
            siteHTML.append("\n\t<a href=\"");
            siteHTML.append("/link");
            siteHTML.append(i);
            siteHTML.append("\"</a>");
        }
        return siteHTML.toString();
    }
}