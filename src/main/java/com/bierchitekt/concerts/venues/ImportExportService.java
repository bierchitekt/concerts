package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
                Document details = Jsoup.connect(link).get();
                Elements select = details.select("div.event-info");


                LocalDate date = LocalDate.parse(select.first().text().substring(4, 12), formatter);

                ConcertDTO concertDTO = new ConcertDTO(title, date, link, null, VENUE_NAME, supportBands, LocalDate.now(), "");
                allConcerts.add(concertDTO);
            }
        } catch (Exception ex) {
            log.error(ex.toString());
        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);
        return allConcerts;
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
