package test;

import main.PageCrawler;
import main.WebCrawler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
public class PageCrawlerTest {
    private static final String SITE_URL = "site_url";
    private static final String FILE_NAME = "file_name";
    @Mock private Connection connection;
    @Mock private Document document;
    private WebCrawler webCrawler;
    private Elements elements;

    @Before
    public void setUp() throws IOException {
        PowerMockito.mockStatic(Jsoup.class);
        connection = Mockito.mock(Connection.class);
        document = Mockito.mock(Document.class);

        webCrawler = new WebCrawler(SITE_URL, FILE_NAME);
        elements = new Elements();

        PowerMockito.when(Jsoup.connect(Mockito.anyString()))
                .thenReturn(connection);
        PowerMockito.when(connection.get()).thenReturn(document);
        PowerMockito.when(document.select(/* cssQuery= */ "a[href]"))
                .thenReturn(elements);
    }

    @Test
    public void pageCrawler_noLinks_noPagesAdded ()
            throws InterruptedException {
        PageCrawler pageCrawler = new PageCrawler(webCrawler,
                webCrawler.getNextUnprocessedPage());

        pageCrawler.run();

        assertEquals(/* expected= */ 0, webCrawler.unprocessedPagesSize());
    }

    @Test
    public void pageCrawler_sameDomainLinks_pagesAdded ()
            throws InterruptedException {

        elements.addAll(buildLinkElements(/* numberOfLinks= */ 2,
                /* numberOfSameDomainLinks= */ 2));
        PageCrawler pageCrawler = new PageCrawler(webCrawler,
                webCrawler.getNextUnprocessedPage());

        pageCrawler.run();

        assertEquals(/* expected= */ 2, webCrawler.unprocessedPagesSize());
    }

    @Test
    public void pageCrawler_differentDomainLinks_noPagesAdded ()
            throws InterruptedException {

        elements.addAll(buildLinkElements(/* numberOfLinks= */2,
                /* numberOfSameDomainLinks= */ 0));
        PageCrawler pageCrawler = new PageCrawler(webCrawler,
                webCrawler.getNextUnprocessedPage());

        pageCrawler.run();

        assertEquals(/* expected= */ 0, webCrawler.unprocessedPagesSize());
    }

    @Test
    public void pageCrawler_mixedDomainLinks_somePagesAdded ()
            throws InterruptedException {

        elements.addAll(buildLinkElements(/* numberOfLinks= */5,
                /* numberOfSameDomainLinks= */ 3));
        PageCrawler pageCrawler = new PageCrawler(webCrawler,
                webCrawler.getNextUnprocessedPage());

        pageCrawler.run();

        assertEquals(/* expected= */ 3, webCrawler.unprocessedPagesSize());
    }

    @Test
    public void pageCrawler_sameDomainRepeatedLinks_repeatedPagesNotAdded ()
            throws InterruptedException {

        elements.addAll(buildLinkElements(/* numberOfLinks= */2,
                /* numberOfSameDomainLinks= */2));
        elements.addAll(buildLinkElements(/* numberOfLinks= */1,
                /* numberOfSameDomainLinks= */ 1));
        PageCrawler pageCrawler = new PageCrawler(webCrawler,
                webCrawler.getNextUnprocessedPage());

        pageCrawler.run();

        assertEquals(/* expected= */ 2, webCrawler.unprocessedPagesSize());
    }

    private List<Element> buildLinkElements
            (int numberOfLinks, int numberOfSameDomainLinks) {
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < numberOfSameDomainLinks; i++) {
            Element element = Mockito.mock(Element.class);
            when(element.attr("abs:href"))
                    .thenReturn(SITE_URL + "/site" + i);
            elements.add(element);
        }

        for (int i = numberOfSameDomainLinks; i < numberOfLinks; i++) {
            Element element = Mockito.mock(Element.class);
            when(element.attr("abs:href")).thenReturn("/site" + i);
            elements.add(element);
        }
        return elements;
    }
}