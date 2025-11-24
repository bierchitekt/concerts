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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MuffathalleService {

    private static final String URL = "https://www.muffatwerk.de/de/events/concert";

    private static final String BASE_URL = "https://www.muffatwerk.de";

    private static final String VENUE_NAME = "Muffathalle";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements allEvents = doc.select("div[id~=event[0-9]+]");
            for (Element event : allEvents) {
                Element first = event.select("div.circle").first();
                if (first != null && !"Konzert".equalsIgnoreCase(first.text())) {
                    continue;
                }
                Element firstElement = event.select("div.entry-data.center").first();

                if (firstElement != null) {
                    String title = firstElement.text().replace("ausverkauft", "").trim();
                    title = title.replaceAll("verlegt auf \\d\\d\\.\\d\\d.\\d\\d ", "").trim();
                    if (title.contains("abgesagt")) {
                        continue;
                    }
                    Elements select = event.select("div.entry-data.right");
                    String link = BASE_URL + select.select("a[href]").getFirst().attr("href");
                    LocalDate date = getDate(event.select("div.date").text());
                    String startTime = event.select("div.entry-data.center").get(1).text();

                    startTime = startTime.substring(startTime.length() - 5);
                    LocalDateTime localTime = LocalDateTime.of(date, LocalTime.parse(startTime));
                    ConcertDTO concertDTO = new ConcertDTO(title, date, localTime, link, null, VENUE_NAME, "", LocalDate.now(), "", "");

                    allConcerts.add(concertDTO);
                }
            }
            log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);

            return allConcerts;
        } catch (Exception ex) {
            log.warn("error while getting {} concerts", VENUE_NAME, ex);
            return List.of();
        }
    }

    private LocalDate getDate(String dateString) {
        try {
            if ("heute".equalsIgnoreCase(dateString)) {
                return LocalDate.now();
            }
            if ("morgen".equalsIgnoreCase(dateString)) {
                return LocalDate.now().plusDays(1);
            }

            String substring = dateString.substring(3);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM yy");
            return LocalDate.parse(substring, formatter);
        } catch (Exception e) {
            log.warn("could not parse date string {} for muffathalle", dateString, e);
            return null;
        }
    }

    public String getPrice(String link) {

        try {
            Document doc = Jsoup.connect(link).get();
            Element element = doc.select("div.entry-data.center").first();
            if (element == null) {
                return "";
            }
            String text = element.text();

            return extractVVKPreis(text);
        } catch (IOException e) {
            log.warn("could not get price for MuffatHalle for {}", link, e);
            return "";
        }
    }

    private String extractVVKPreis(String input) {
        String pattern = "VVK €\\s*(\\d+)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(input);

        if (m.find()) {
            return m.group(1) + " €";
        } else {
            return "";
        }
    }
}
