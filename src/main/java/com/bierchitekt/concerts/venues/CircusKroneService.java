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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
                    Integer month;
                    try {
                        month = getMonth(dateString);
                    } catch (UnknownDateException e) {
                        continue;
                    }

                    if (month == null) {
                        continue;
                    }
                    int year = Integer.parseInt(StringUtils.substringAfterLast(dateString, " "));

                    if (dateString.contains("/")) {
                        List<String> dates = Arrays.stream(dateString.split("/")).toList();

                        for (String d : dates) {
                            int day = Integer.parseInt(d.substring(0, 2));

                            LocalDate date = LocalDate.of(year, month, day);
                            ConcertDTO concertDTO = new ConcertDTO(title, date, link, null, "Circus Krone", "", LocalDate.now(), "");
                            allConcerts.add(concertDTO);
                        }
                    } else {
                        int day = Integer.parseInt(dateString.substring(0, 2));
                        LocalDate date = LocalDate.of(year, month, day);

                        ConcertDTO concertDTO = new ConcertDTO(title, date, link, null, "Circus Krone", "", LocalDate.now(), "");
                        allConcerts.add(concertDTO);

                    }
                }
            }
        } catch (IOException e) {
            if (!(e instanceof HttpStatusException)) {
                log.warn("error getting concerts from Circus Krone", e);
            }
        }
        return allConcerts;
    }

    private Integer getMonth(String dateString) throws UnknownDateException {
        String s = StringUtils.substringBetween(dateString, " ", " ");
        if (s == null) {
            log.warn("cannot parse date {}", dateString);
            throw new UnknownDateException(dateString);
        }
        Integer i = calendarMap.get(s);
        if (i == null) {
            log.warn("Cannot get month for input {}", dateString);
        }
        return i;
    }

    private static class UnknownDateException extends Exception {
        public UnknownDateException(String foo) {
            super(foo);
        }
    }
}