package com.searchengine.model;

import java.util.ArrayList;
import java.util.List;

public class Document {
    private final String url;
    private String title;
    private String rawHtml;
    private String cleanedText;
    private final List<String> outLinks;
    private long crawlTimestamp;
    private int httpStatusCode;

    public Document(String url) {
        this.url = url;
        this.outLinks = new ArrayList<>();
    }

    public String getUrl() { return url; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getRawHtml() { return rawHtml; }
    public void setRawHtml(String rawHtml) { this.rawHtml = rawHtml; }

    public String getCleanedText() { return cleanedText; }
    public void setCleanedText(String cleanedText) { this.cleanedText = cleanedText; }

    public List<String> getOutLinks() { return outLinks; }
    public void addOutLink(String link) { this.outLinks.add(link); }

    public long getCrawlTimestamp() { return crawlTimestamp; }
    public void setCrawlTimestamp(long crawlTimestamp) {
        this.crawlTimestamp = crawlTimestamp;
    }

    public int getHttpStatusCode() { return httpStatusCode; }
    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    @Override
    public String toString() {
        return "Document[url=" + url + ", title=" + title +
                ", links=" + outLinks.size() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document doc)) return false;
        return url.equals(doc.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}