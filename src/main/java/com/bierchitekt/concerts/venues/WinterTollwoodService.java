package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bierchitekt.concerts.venues.Venue.WINTER_TOLLWOOD;


@Slf4j
@Service
public class WinterTollwoodService {

    private static final String URL = "https://www.tollwood.de/kalender-winter-2025/#category=tax-79&list=show/";

    public  static final String VENUE_NAME = WINTER_TOLLWOOD.name();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);
        List<ConcertDTO> allConcerts = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(URL).get();
            Set<String> concertLinks = getConcertLinks(doc);
            for (String concertLink : concertLinks) {
                Document elements = Jsoup.connect(concertLink).get();


                Elements concertDetails = elements.select("div.inside-infoblock");

                Element titleElement = concertDetails.select("h1").first();
                if (titleElement == null) {
                    continue;
                }
                String title = titleElement.text();

                String dateString = concertDetails.select("h2").text().substring(0, 10);

                String startTime = concertDetails.select("h2").text();
                startTime = StringUtils.substringBetween(startTime, " | ", " Uhr");
                if (startTime.length() == 2) {
                    startTime = startTime + ":00";
                }
                LocalDate date = LocalDate.parse(dateString, formatter);

                Elements concertDetails1 = elements.select("div.column-main");
                Elements select = concertDetails1.select("h1");
                String genre = "";
                for (Element element : select) {
                    if (!Strings.CI.contains(element.text(), "Konzert")) {
                        genre = element.text();
                        break;
                    }
                    if (genre.equalsIgnoreCase("")) {
                        Element firstGenre = elements.select("h3").first();
                        if (firstGenre != null) {
                            genre = firstGenre.text();
                        }
                    }
                }

                Element priceElement = elements.select("span.icon-star").parents().first();
                String price = "";
                if (priceElement != null) {
                    price = priceElement.text();
                }
                String[] split = genre.split("[,&]");
                Set<String> allGenres = new HashSet<>();
                for (String genres : split) {
                    allGenres.add(genres.trim());
                }
                LocalDateTime dateAndTime = LocalDateTime.of(date, LocalTime.parse(startTime));
                ConcertDTO concertDTO = new ConcertDTO(title, date, dateAndTime, concertLink, allGenres, VENUE_NAME, "", LocalDate.now(), price, "");
                allConcerts.add(concertDTO);
            }
        } catch (Exception e) {
            log.warn("error getting {} concerts", VENUE_NAME, e);
            return allConcerts;
        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);
        return allConcerts;
    }

    private Set<String> getConcertLinks(Document doc) {
        Set<String> links = new HashSet<>();
        Elements allLinks = doc.select("tr");

        for (Element element : allLinks) {
            Elements categories = element.select("td.cell-category");
            if (categories.isEmpty() || !categories.text().contains("Musik")) {
                continue;
            }
            String concertLink = element.select("a").attr("href");
            links.add(concertLink);
        }
        return links;
    }
}
