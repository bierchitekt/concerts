package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class Theaterfabrik {

    private static final String URL = "https://theaterfabrik-muc.de/line-up/";

    private static final String VENUE_NAME = "Theaterfabrik";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public List<ConcertDTO> getConcerts() {
        try {
            log.info("getting {} concerts", VENUE_NAME);

            List<ConcertDTO> allConcerts = new ArrayList<>();
            Document doc = Jsoup.connect(URL).get();
            Elements allElements = doc.select("div.elementor-post__card");
            for (Element concert : allElements) {
                String title = concert.select("h3.elementor-post__title").text();
                title = StringUtils.substringBefore(title, "+");
                title = StringUtil.capitalizeWords(title);
                String link = concert.select("a[href]").getFirst().attr("href");
                String concertDetail = concert.select("div.elementor-post__excerpt").text();

                String dateString = StringUtils.substringBetween(concertDetail, "Datum: ", "Einlass:");

                List<LocalDate> dates = new ArrayList<>();
                if (dateString == null) {
                    dates.add(LocalDate.parse(StringUtils.substringBetween(concertDetail, "Datum 1: ", " Datum 2:"), formatter));
                    dates.add(LocalDate.parse(StringUtils.substringBetween(concertDetail, "Datum 2: ", " Einlass"), formatter));
                }

                String price = StringUtils.substringBetween(concertDetail, "Ticketpreis: ", "€");
                price += "€";

                if (dates.isEmpty()) {
                    // some dates are missing the complete year. 14.09.25 => 14.09.2024
                    if (dateString.length() == 9) {
                        dateString = new StringBuilder(dateString).insert(dateString.length() - 3, "20").toString();
                    }
                    LocalDate date = LocalDate.parse(dateString.trim(), formatter);
                    ConcertDTO concertDTO = new ConcertDTO(title, date, link, null, VENUE_NAME, price);
                    allConcerts.add(concertDTO);
                } else {
                    for (LocalDate singleDate : dates) {
                        ConcertDTO concertDTO = new ConcertDTO(title, singleDate, link, null, VENUE_NAME, price);
                        allConcerts.add(concertDTO);

                    }
                }
            }
            log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);

            return allConcerts;
        } catch (Exception ex) {
            log.warn("exception on {}", VENUE_NAME, ex);
            return List.of();
        }
    }
}