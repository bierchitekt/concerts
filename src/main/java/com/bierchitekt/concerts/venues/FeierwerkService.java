package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

import static com.bierchitekt.concerts.ConcertService.CALENDAR_URL;
import static com.bierchitekt.concerts.venues.Venue.FEIERWERK;

@Service
@Slf4j
public class FeierwerkService {

    public static final String VENUE_NAME = FEIERWERK.getName();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String BASE_URL = "https://www.feierwerk.de";

    public Set<String> getConcertLinks() {
        Set<String> concertLinks = new HashSet<>();

        try {
            String url = "https://www.feierwerk.de/konzert-kulturprogramm/kkp?tx_search_copilotsearch%5Baction%5D=loadMoreEvents&tx_search_copilotsearch%5Bcontroller%5D=Data&tx_search_copilotsearch%5Boffset%5D=0&tx_search_copilotsearch%5Blimit%5D=200";

            Document doc = Jsoup.connect(url).get();

            Elements concerts = doc.select("a[href]");
            for (Element concert : concerts) {
                String href = concert.attr("href");
                if (href.startsWith("/konzert-kulturprogramm/detail/")) {
                    concertLinks.add(BASE_URL + href);
                }
            }
        } catch (Exception ex) {
            log.warn("exception", ex);
        }
        log.info("found {} {} links", concertLinks.size(), VENUE_NAME);
        return concertLinks;
    }

    public Optional<ConcertDTO> getConcert(String url) {

        try {
            Document doc = Jsoup.connect(url).get();
            List<String> bands = getBands(doc);
            LocalDate date = getDate(doc);
            String startTime = getTime(doc);
            Set<String> genres = getGenres(doc);
            if (bands.isEmpty()) {
                return Optional.empty();
            }

            for (String genre : genres) {
                if (genre.contains("Ausstellung") || genre.contains("Malerei") || genre.contains("Illustrationen") || genre.contains("Workshops") || genre.contains("Literatur")) {
                    return Optional.empty();
                }
            }
            String supportBands = String.join(", ", bands);
            String price = getPrice(doc);

            LocalDateTime dateAndTime = LocalDateTime.of(date, LocalTime.parse(startTime));

            return Optional.of(new ConcertDTO(bands.getFirst(), date, dateAndTime, url, genres, VENUE_NAME, supportBands, LocalDate.now(), price,
                    CALENDAR_URL + StringUtil.getICSFilename(bands.getFirst(), date)));
        } catch (Exception _) {
            return Optional.empty();
        }
    }

    private String getPrice(Document doc) {
        Elements select = doc.select("div.additional-info");
        return StringUtils.substringBetween(select.text(), "VVK: ", " EURO") + " €";
    }

    private Set<String> getGenres(Document doc) {
        String genres = doc.select("span.artist-genre").text();
        if (genres.startsWith("|")) {
            genres = genres.substring(1);
        }
        genres = genres.trim();
        String[] split = genres.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return Set.of(split);
    }

    private LocalDate getDate(Document doc) {
        String date = doc.select("span.date-bold").text();
        return LocalDate.parse(date, formatter);
    }

    private String getTime(Document doc) {
        String date = doc.select("div.additional-info").text();
        return StringUtils.substringBetween(date.toLowerCase(), "beginn:", "uhr").trim();
    }


    private List<String> getBands(Document doc) {
        List<String> bands = new ArrayList<>();
        try {

            Elements select = doc.select("span.artist-name");
            for (Element band : select) {
                bands.add(band.text());
            }

        } catch (Exception ex) {
            log.warn("exception: ", ex);
        }
        return bands;
    }
}
