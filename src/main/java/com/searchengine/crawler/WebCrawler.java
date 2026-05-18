package com.searchengine.crawler;

import com.searchengine.model.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.time.Instant;

public class WebCrawler {
    private static final int POLITENESS_DELAY_MS = 1000;
    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; SearchEngine/1.0; student-project)";

    public Document crawl(String url) throws IOException {
        System.out.println("Crawling: " + url);

        org.jsoup.nodes.Document jsoupDoc = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT_MS)
                .followRedirects(true)
                .get();

        Document doc = new Document(url);
        doc.setTitle(jsoupDoc.title());
        doc.setRawHtml(jsoupDoc.html());
        doc.setCrawlTimestamp(Instant.now().toEpochMilli());
        doc.setHttpStatusCode(200);

        String visibleText = jsoupDoc.body().text();
        doc.setCleanedText(visibleText);

        Elements links = jsoupDoc.select("a[href]");
        for (Element link : links) {
            String absHref = link.absUrl("href");
            if (!absHref.isEmpty() && absHref.startsWith("http")) {
                doc.addOutLink(absHref);
            }
        }

        System.out.println("  → Title: " + doc.getTitle());
        System.out.println("  → Text: " + doc.getCleanedText().length() + " chars");
        System.out.println("  → Links: " + doc.getOutLinks().size());

        return doc;
    }
    public void politeDelay(String url) {
        try {
            Thread.sleep(POLITENESS_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}