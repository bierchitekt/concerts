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
                if (!"Konzert".equalsIgnoreCase(event.select("div.circle").first().text())) {
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
                    ConcertDTO concertDTO = new ConcertDTO(title, date, link, null, VENUE_NAME, "", LocalDate.now(), "");

                    allConcerts.add(concertDTO);
                }
            }
            log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);

            return allConcerts;
        } catch (Exception ex) {
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
            return null;
        }
    }

    public String getPrice(String link) {

        try {
            Document doc = Jsoup.connect(link).get();
            String text = doc.select("p.entry-info").first().text();

            return extractVVKPreis(text);
        } catch (IOException e) {
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
