package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class Kult9Service {

    private static final String URL = "https://www.kult9.de/";

    private static final String VENUE_NAME = "Kult9";

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. LLLL yyyy");

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {

            Document doc = Jsoup.connect(URL).get();

            Elements allEvents = doc.select("div.type_live");

            for (Element event : allEvents) {
                String title = event.select("h4").text();
                title = title.replace("Abgesagt - ", "");
                title = title.replace(" - AUSVERKAUFT!", "");

                if ("Kult9 Live".equalsIgnoreCase(title) || "Musik & Kleinkunst im Kult9".equalsIgnoreCase(title)) {
                    continue;
                }
                String genre = event.select("p").first().text();
                String[] split = genre.split(",");
                Set<String> allGenres = new HashSet<>();

                for (String genres : split) {
                    allGenres.add(genres.trim());
                }
                String day = event.select("div.day").text();

                day = day.substring(4);
                LocalDate date = LocalDate.parse(day, formatter);
                Elements linkElement = event.select("div.btnarea").select("a[href]");
                String link = "";
                if (!linkElement.isEmpty()) {
                    link = event.select("div.btnarea").select("a[href]").getFirst().attr("href");
                }
                String price = "";
                ConcertDTO concertDTO = new ConcertDTO(title, date, link, allGenres, VENUE_NAME, "", LocalDate.now(), price);
                allConcerts.add(concertDTO);
            }
        } catch (Exception ex) {
            log.warn("error getting {} concerts", VENUE_NAME, ex);
            return List.of();
        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);

        return allConcerts;
    }
}
