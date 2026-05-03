package com.training.app.embedding.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HtmlTableExtractorService {

    public String extractTablesAsHtml(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        Elements tables = document.select("table");

        StringBuilder tableHtml = new StringBuilder();
        for (Element table : tables) {
            tableHtml.append(table.outerHtml()).append("\n");
        }

        return tableHtml.toString();
    }
}

