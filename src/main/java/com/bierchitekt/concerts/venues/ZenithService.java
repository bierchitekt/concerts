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
public class ZenithService {

    String url = "https://muenchen.motorworld.de/";

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    List<String> ignoredEvents = List.of("Midnightbazar", "Kinky Galore");

    private static final String VENUE_NAME = "Zenith";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);
        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {

            Document document = Jsoup.connect(url).get();
            Elements allEvents = document.select("a.elementor-element.e-flex.e-con-boxed.e-con.e-parent");
            for (Element concert : allEvents) {


                if ("Programm Motorworld München".equals(concert.text())) {
                    continue;
                }
                String title = concert.select("h1.elementor-heading-title.elementor-size-default").text();

                title = title.replace("(ausverkauft)", "").trim();
                title = title.replace("(Doppelshow)", "").trim();
                title = title.replace("(Zusatzshow)", "").trim();

                title = StringUtil.capitalizeWords(title);
                if (ignoredEvents.contains(title)) {
                    continue;
                }
                String link = concert.select("a[href]").getFirst().attr("href");
                Elements details = concert.select("div.elementor-element.elementor-widget.elementor-widget-text-editor");

                LocalDate date = getDate(details);

                if (date == null) {
                    continue;
                }

                ConcertDTO concertDTO = new ConcertDTO(title, date, link, null, VENUE_NAME, null);
                allConcerts.add(concertDTO);
            }
        } catch (Exception ex) {
            log.warn("Error getting {} concerts", VENUE_NAME, ex);
            return allConcerts;
        }
        log.info("received {} {} concerts", VENUE_NAME, allConcerts.size());
        return allConcerts;
    }

    private LocalDate getDate(Elements details) {
        for (Element dateElement : details) {
            try {
                return LocalDate.parse(dateElement.text(), formatter);
            } catch (Exception ignored) {
                // ignored
            }
        }
        return null;
    }
}
