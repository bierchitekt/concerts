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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.bierchitekt.concerts.venues.Venue.BACKSTAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackstageService {

    private static final String URL = "https://www.backstage.eu";
    private static final String OVERVIEW_URL = URL + "/events";

    public static final String VENUE_NAME = BACKSTAGE.getName();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");


    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);
        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {

            Document document = Jsoup.connect(OVERVIEW_URL).get();


            Elements allEvents = document.select("a.my-5");

            for (Element event : allEvents) {
                Optional<ConcertDTO> concert = getConcert(event);
                concert.ifPresent(allConcerts::add);

            }

            log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);
            return allConcerts;
        } catch (Exception ex) {
            log.error("exception: ", ex);
            return allConcerts;
        }
    }

    private Optional<ConcertDTO> getConcert(Element event) {
        Set<String> genres = new HashSet<>();
        String link = URL + event.select("a[href]").getFirst().attr("href");
        Elements select = event.select("p.Text_headline4__NpUZq");
        Elements genreElements = event.select("p.Text_text5__NsFNS.break-words.w-fit.h-fit");
        for (Element genre : genreElements) {
            String genreText = genre.text();
            String genreTextLowerCase = genreText.toLowerCase();
            if (genreTextLowerCase.isEmpty() || genreTextLowerCase.contains("fussball") || genreTextLowerCase.contains("party")
                    || genreTextLowerCase.contains("biergarten") || genreTextLowerCase.contains("lesung") ||
                    genreTextLowerCase.contains("pop! reloaded") || genreTextLowerCase.contains("rollschuh") ||
                    genreTextLowerCase.contains("caribbean vibes")
            ) {
                return Optional.empty();
            }
            genres.add(genreText);
        }

        String title = StringUtil.capitalizeWords(select.text());
        if (title.toLowerCase().contains("rollschuh") || title.toLowerCase().contains("caribbean vibes")) {
            return Optional.empty();
        }
        Elements select1 = event.select("p.Text_text5__NsFNS.w-fit.h-fit");
        String eventDate = select1.getLast().text();
        LocalDate date = LocalDate.parse(eventDate.substring(3, 13), formatter);
        if (date.isBefore(LocalDate.now())) {
            return Optional.empty();
        }
        String[] timeAndLocationAndPrice = event.select("p.Text_text7__vd_Lx.w-fit.h-fit").text().split("\\|");
        String startTime = timeAndLocationAndPrice[0].trim();
        String location = StringUtil.capitalizeWords(timeAndLocationAndPrice[1].trim());
        String price = "";
        if (timeAndLocationAndPrice.length > 2) {
            price = timeAndLocationAndPrice[2].trim();
        }
        LocalDateTime dateAndTime = LocalDateTime.of(date, LocalTime.parse(startTime));

        ConcertDTO concertDTO = new ConcertDTO(title, date, dateAndTime, link, genres, location, null, LocalDate.now(), price, "");
        return Optional.of(concertDTO);
    }
}
