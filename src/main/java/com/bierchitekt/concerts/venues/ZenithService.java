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
                String title = getTitle(concert);
                if (ignoredEvents.contains(title)) {
                    continue;
                }
                String link = concert.select("a[href]").getFirst().attr("href");
                Elements details = concert.select("div.elementor-element.elementor-widget.elementor-widget-text-editor");

                LocalDate date = getDate(details);

                if (date == null) {
                    continue;
                }

                ConcertDTO concertDTO = new ConcertDTO(title, date, null, link, null, VENUE_NAME, "", LocalDate.now(), "");
                allConcerts.add(concertDTO);
            }
        } catch (Exception ex) {
            log.warn("Error getting {} concerts", VENUE_NAME, ex);
            return allConcerts;
        }
        log.info("received {} {} concerts", VENUE_NAME, allConcerts.size());
        return allConcerts;
    }

    private String getTitle(Element concert) {
        String title = concert.select("h1.elementor-heading-title.elementor-size-default").text();

        title = title.replace("(ausverkauft)", "").trim();
        title = title.replace("(Doppelshow)", "").trim();
        title = title.replace("(Zusatzshow)", "").trim();

        title = StringUtils.substringBefore(title, " x ").trim();
        title = StringUtils.substringBefore(title, " + ").trim();

        return StringUtil.capitalizeWords(title);
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

    public String getTime(String link) {
        try {
            Document doc = Jsoup.connect(link).get();
            String text = doc.select("div.elementor-widget-container").text();
            return StringUtils.substringBetween(text, "Beginn: ", " Uhr").trim();
        } catch (IOException e) {
            log.warn("error getting time for zenith url {} ", "link", e);
            return "";
        }

    }

    public String getSupportBands(String link) {
        try {
            Document doc = Jsoup.connect(link).get();
            Elements select = doc.select("div.elementor-widget-container");
            for (Element element : select) {

                String supportText = "Support: ";
                if (element.text().contains(supportText)) {
                    Elements select1 = element.select("p");
                    for (Element support : select1) {
                        if (support.text().contains(supportText)) {
                            return StringUtils.substringAfter(support.text(), supportText).trim();
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.warn("error getting price for backstage url {} ", "link", e);

        }
        return "";
    }

}
