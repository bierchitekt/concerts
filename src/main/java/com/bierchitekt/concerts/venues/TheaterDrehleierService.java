package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
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
import java.util.Locale;
import java.util.Optional;

import static com.bierchitekt.concerts.venues.Venue.THEATERDREHLEIER;

@Slf4j
@Service
public class TheaterDrehleierService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN);
    private static final List<String> SEPARATORS = List.of("+", "//");
    public static final String VENUE_NAME = THEATERDREHLEIER.getName();

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {
            String url = "https://theater-drehleier.de/programm/event/filter/genre/konzert";
            Document doc = Jsoup.connect(url).get();
            Elements concerts = doc.select("div.col-sm-12.col-md-6.col-lg-4.p-3");

            for (Element concert : concerts) {
                Elements select = concert.select("div.card-body");
                String text = select.select("h3").text();
                String title = StringUtil.capitalizeWords(text);

                if (title.startsWith("Notenlos") || title.startsWith("Sing Salong")) {
                    continue;
                }

                String link = concert.select("a[href]").getFirst().attr("href");

                String completeDate = select.select("div:not([class])").text();
                LocalDate date = LocalDate.parse(completeDate, formatter);


                String einlass = select.select("p").text();
                String beginn = StringUtils.substringBetween(einlass, "Einlass ", " - Beginn ");

                LocalDateTime dateAndTime = LocalDateTime.of(date, LocalTime.parse(beginn));
                Pair bands = getBands(title);

                allConcerts.add(new ConcertDTO(bands.mainAct, date, dateAndTime, link, null, "Drehleier", bands.supportBands, LocalDate.now(), "", ""));
            }


        } catch (IOException e) {
            log.warn("error getting concerts for {}", VENUE_NAME, e);
        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);
        return allConcerts;
    }

    private Pair getBands(String title) {
        for (String s : SEPARATORS) {
            Optional<Pair> bandsWithSeparator = getBandsWithSeparator(title, s);
            if (bandsWithSeparator.isPresent())
                return bandsWithSeparator.get();

        }
        return new Pair(title.trim(), "");
    }

    private Optional<Pair> getBandsWithSeparator(String title, String separator) {
        if (title.contains(separator)) {
            if ("+".equals(separator)) {
                separator = "\\+";
            }
            String[] bands = title.split(separator);
            StringBuilder supportBands = new StringBuilder();
            for (int i = 1; i < bands.length; i++) {
                supportBands.append(bands[i]).append(", ");
            }

            return Optional.of(new Pair(bands[0].trim(), supportBands.substring(0, supportBands.length() - 2).trim()));
        }
        return Optional.empty();
    }


    private record Pair(String mainAct, String supportBands) {
    }
}
