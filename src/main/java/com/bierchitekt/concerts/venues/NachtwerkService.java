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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.bierchitekt.concerts.venues.Venue.NACHTWERK;

@Slf4j
@Service
public class NachtwerkService {

    public static final String URL = "https://www.nachtwerk.de";
    public static final String VENUE_NAME = NACHTWERK.getName();
    private static final DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .parseLenient() // Handles 'Sa' vs 'Sa.' variations
            .appendPattern("EEE, d. MMMM yyyy")
            .toFormatter(Locale.GERMAN);

    public List<ConcertDTO> getConcerts() {
        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {
            Document document = Jsoup.connect(URL).get();
            Elements allEvents = document.select("li.concert-card");

            for (Element event : allEvents) {
                // Extract text or fallback to empty string if element is missing
                String title = event.select(".title, .event-title, h2, h3").text();

                Pair bands = getSupportBands(title);
                String link = URL + event.select("a[href]").getFirst().attr("href");

                Elements dateAndTimeElements = event.select("dd");
                LocalDate date = null;
                LocalDateTime dateAndTime = null;
                if (dateAndTimeElements.size() > 2) {
                    date = LocalDate.parse(dateAndTimeElements.getFirst().text(), formatter);
                    dateAndTime = LocalDateTime.of(date, LocalTime.parse(dateAndTimeElements.get(1).text()));
                }

                ConcertDTO concertDTO = new ConcertDTO(bands.title, date, dateAndTime, link, null, VENUE_NAME, bands.supportBands, LocalDate.now(), "", "");
                allConcerts.add(concertDTO);
            }
        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
        }

        return allConcerts;
    }

    private Pair getSupportBands(String title) {
        if (title.contains("Support")) {
            return new Pair(title.replaceFirst("-.*$", "").trim(),
                    title.replaceFirst("(?i)^.*-\\s*supports?:\\s*", "").trim());
        }

        return new Pair(title, "");
    }

    private record Pair(String title, String supportBands) {
    }
}
