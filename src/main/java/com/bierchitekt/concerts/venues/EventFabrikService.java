package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class EventFabrikService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public List<ConcertDTO> getConcerts() {
        List<ConcertDTO> allConcerts = new ArrayList<>();

        try {

            int pages = getNumberOfPages();

            for (int page = 1; page <= pages; page++) {
                String url = "https://www.eventfabrik-muenchen.de/events/?tribe_paged=" + page + "&s&tribe_events_cat=konzert&tribe_events_venue&tribe_events_month&hide_canceled=on";

                Document doc = Jsoup.connect(url).get();

                Elements concerts = doc.select("article.archive-card.type-tribe_events");

                for (Element concert : concerts) {

                    String title = StringUtil.capitalizeWords(Objects.requireNonNull(concert.select("a").first()).attr("title"));

                    title = title.replace(" – Verlegt", "");
                    String link = Objects.requireNonNull(concert.select("a[href]").first()).attr("href");
                    String location = concert.select("li.archive-card--meta-venue").text();
                    // Do. | 19.12.2024
                    String dateString = concert.select("article").text();
                    LocalDate date = LocalDate.parse(dateString.substring(6, 16), formatter);
                    ConcertDTO concertDTO = new ConcertDTO(title, date, link, null, location, "", LocalDate.now());
                    allConcerts.add(concertDTO);
                }

            }
        } catch (IOException e) {

            log.error("error getting eventfabrik concerts", e);
            return allConcerts;
        }

        return allConcerts;

    }

    private int getNumberOfPages() throws IOException {
        String url = "https://www.eventfabrik-muenchen.de/events/?tribe_paged=1&s&tribe_events_cat=konzert&tribe_events_venue&tribe_events_month";
        Document doc = Jsoup.connect(url).get();
        Elements select = doc.select("a.page-numbers");

        return Integer.parseInt(select.get(1).text());
    }

}
