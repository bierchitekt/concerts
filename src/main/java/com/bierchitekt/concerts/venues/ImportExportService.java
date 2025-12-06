package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportExportService {

    private static final String URL = "https://import-export.cc/programm/";

    private static final String VENUE_NAME = "Import Export";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy");

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {

            Document doc = Jsoup.connect(URL).get();
            Elements allEvents = doc.select("div.event-link");
            for (Element concert : allEvents) {
                String text = concert.text();
                if (!text.contains("Konzert") || !concert.select("div.old").isEmpty()) {
                    continue;
                }

                String title = concert.select("h2.io-title").text();
                title = getTitle(title);

                String[] result = title.split(" \\+ ");
                String supportBands = "";
                if (result.length > 1) {
                    title = result[0];
                    supportBands = String.join(", ", result[1].split(" \\+ "));
                }

                String link = concert.parent().select("a[href]").getFirst().attr("href");
                if (linkExists(allConcerts, link)) {
                    continue;
                }

                Optional<Document> details = getDetails(link);
                if(details.isEmpty()) {
                    continue;
                }
                Elements select = details.get().select("div.event-info");


                LocalDate date = LocalDate.parse(select.first().text().substring(4, 12), formatter);
                String startTime = StringUtils.substringAfter(select.first().text(), "Beginn:").trim();
                LocalDateTime localDateTime = LocalDateTime.of(date, LocalTime.parse(startTime));
                ConcertDTO concertDTO = new ConcertDTO(title, date, localDateTime, link, null, VENUE_NAME, supportBands, LocalDate.now(), "", "");
                allConcerts.add(concertDTO);
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);
        return allConcerts;
    }

    private Optional<Document> getDetails(String link) {

        for (int i = 0; i < 5; i++) {
            try {
                return Optional.of(Jsoup.connect(link).get());
            } catch (IOException _) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        log.warn("error getting concert details for {} for url {}", VENUE_NAME, link);
        return Optional.empty();
    }

    private String getTitle(String title) {
        String[] result = title.split("pres.: ");

        if (result.length > 1) {
            return result[1];
        }
        result = title.split("Production: ");

        if (result.length > 1) {
            return result[1];
        }
        result = title.split("KreativLabor Open: ");
        if (result.length > 1) {
            return result[1];
        }

        return title;

    }

    private boolean linkExists(List<ConcertDTO> concerts, String link) {
        for (ConcertDTO concert : concerts) {
            if (concert.link().equalsIgnoreCase(link)) {
                return true;
            }
        }
        return false;
    }
}
