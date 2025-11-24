package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class EventFabrikService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public List<ConcertDTO> getConcerts() {
        List<ConcertDTO> allConcerts = new ArrayList<>();

        try {

            int pages = getNumberOfPages();

            for (int page = 1; page <= pages; page++) {
                String url = "https://www.eventfabrik-muenchen.de/event/page/" + page + "/?search&category=konzert&location&month";
                Document doc = Jsoup.connect(url).get();

                doc.select("script.yoast-schema-graph");
                String result = doc.select("script.yoast-schema-graph").getFirst().childNodes().getFirst().toString();

                JsonArray hits = JsonParser.parseString(result).getAsJsonObject()
                        .get("@graph").getAsJsonArray();

                for (JsonElement concert : hits) {
                    JsonElement en = concert.getAsJsonObject().get("@type");
                    if ("\"Event\"".equals(en.toString())) {
                        String title = concert.getAsJsonObject().get("name").getAsString().trim();
                        title = StringUtil.capitalizeWords(title);
                        String price = getPrice(concert);

                        LocalDate date = LocalDate.parse(concert.getAsJsonObject().get("startDate").getAsString().substring(0, 10), formatter);
                        String startTime = concert.getAsJsonObject().get("startDate").getAsString().substring(11, 16);
                        String link = concert.getAsJsonObject().get("url").getAsString();

                        LocalDateTime dateAndTime = LocalDateTime.of(date, LocalTime.parse(startTime));
                        ConcertDTO concertDTO = new ConcertDTO(title, date, dateAndTime, link, null, "EventFabrik", "", LocalDate.now(), price);
                        allConcerts.add(concertDTO);
                    }
                }

            }
        } catch (IOException e) {

            log.error("error getting eventfabrik concerts", e);
            return allConcerts;
        }

        return allConcerts;

    }

    private String getPrice(JsonElement concert) {
        BigDecimal price = new BigDecimal(concert.getAsJsonObject().get("offers").getAsJsonObject().get("price").getAsString());
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        if (price.scale() <= 0) {
            return price.toPlainString() + " €";
        } else {
            BigDecimal truncated = price.setScale(2, RoundingMode.DOWN);
            return truncated.toPlainString() + " €";
        }
    }

    private int getNumberOfPages() throws IOException {
        String url = "https://www.eventfabrik-muenchen.de/event/?search&category=konzert&location&month";
        Document doc = Jsoup.connect(url).get();
        Elements select = doc.select("a.page-numbers");

        return Integer.parseInt(select.get(1).text());
    }
}

