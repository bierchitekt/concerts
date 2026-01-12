package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class CircusKroneService {

    private static final Map<String, Integer> calendarMap = Map.ofEntries(
            Map.entry("Januar", 1),
            Map.entry("Februar", 2), Map.entry("Feb", 2),
            Map.entry("März", 3),
            Map.entry("April", 4), Map.entry("Apr", 4), Map.entry("Apr.", 4),
            Map.entry("Mai", 5),
            Map.entry("Juni", 6),
            Map.entry("Juli", 7),
            Map.entry("August", 8),
            Map.entry("Sept.", 9),
            Map.entry("Okt.", 10), Map.entry("Okt", 10),
            Map.entry("Nov.", 11),
            Map.entry("Dezember", 12));

    public static final String VENUE_NAME = Venue.CIRCUSKRONE.getName();

    public List<ConcertDTO> getConcerts() {
        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {
            for (int page = 1; page < 100; page++) {
                String url = "https://bau.circus-krone.com/alle-veranstaltungen/konzerte/page/" + page;
                Document doc = Jsoup.connect(url).get();
                Elements concerts = doc.select("article.fusion-portfolio-post");
                for (Element concert : concerts) {
                    String title = StringUtil.capitalizeWords(concert.select("h4").text());
                    String link = Objects.requireNonNull(concert.select("a[href]").first()).attr("href");

                    String dateString = concert.select("div.fusion-post-content").text();
                    Optional<Integer> monthOptional = getMonth(dateString);

                    if (monthOptional.isEmpty()) {
                        continue;
                    }
                    int month = monthOptional.get();
                    int year = Integer.parseInt(StringUtils.substringAfterLast(dateString, " "));


                    List<Integer> days = getDays(dateString);


                    for (Integer day : days) {
                        LocalDate date = LocalDate.of(year, month, day);
                        ConcertDTO concertDTO = new ConcertDTO(title, date, null, link, null, VENUE_NAME, "", LocalDate.now(), "", "");
                        allConcerts.add(concertDTO);
                    }
                }
            }
        } catch (
                Exception e) {
            if (!(e instanceof HttpStatusException)) {
                log.warn("error getting concerts from {}}", VENUE_NAME, e);
            }
        }
        return allConcerts;
    }

    private List<Integer> getDays(String dateString) {
        List<Integer> days = new ArrayList<>();
        if (dateString.contains("/")) {
            String[] split = dateString.split("/");
            for (String s : split) {
                days.add(Integer.parseInt(s.substring(0, 2)));
            }
        } else {
            days.add(Integer.parseInt(dateString.substring(0, 2)));
        }

        return days;
    }

    public LocalTime getBeginn(String link) {
        try {
            Document doc = Jsoup.connect(link).get();
            Element textElement = doc.select("div.fusion-text").first();
            if (textElement == null) {
                log.warn("error getting price for circusKrone url {} ", link);
                return LocalTime.of(19, 0);
            }
            String text = textElement.text();

            String beginn = StringUtils.substringBetween(text, "Beginn: ", " Uhr");
            if (beginn == null) {
                beginn = StringUtils.substringBetween(text, "Einlass: ", " Uhr");
            }
            return LocalTime.parse(beginn);
        } catch (IOException e) {
            log.warn("error getting price for circusKrone url {} ", link, e);
            return LocalTime.of(19, 0);
        }

    }

    private Optional<Integer> getMonth(String dateString) {
        String s = StringUtils.substringBetween(dateString, " ", " ");
        if (s == null) {
            log.warn("cannot parse date {}", dateString);
            return Optional.empty();
        }
        Integer i = calendarMap.get(s);
        if (i == null) {
            log.warn("Cannot get month for input {}", dateString);
            return Optional.empty();
        }
        return Optional.of(i);
    }
}