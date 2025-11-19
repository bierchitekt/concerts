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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class FeierwerkService {

    private static final String VENUE_NAME = "Feierwerk";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String BASE_URL = "https://www.feierwerk.de";

    public Set<String> getConcertLinks() {
        Set<String> concertLinks = new HashSet<>();

        try {
            for (String url : getLinks()) {
                Document doc = Jsoup.connect(url).get();

                Elements concerts = doc.select("a[href]");
                for (Element concert : concerts) {
                    String href = concert.attr("href");
                    if (href.startsWith("/konzert-kulturprogramm/detail/")) {
                        concertLinks.add(BASE_URL + href);
                    }
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

            return Optional.of(new ConcertDTO(bands.getFirst(), date, url, genres, VENUE_NAME, supportBands, LocalDate.now(), price));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private String getPrice(Document doc) {
        Elements select = doc.select("#top > div > div.event-date-location-detail > div > span");

        return extractPrice(select.text());
    }

    public String extractPrice(String input) {

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group() + " €";
        }
        return "";
    }

    private Set<String> getLinks() throws IOException {
        String url = "https://www.feierwerk.de/konzert-kulturprogramm/kkp";

        Document doc = Jsoup.connect(url).get();

        Elements select = doc.select("ul.f3-widget-paginator").select("a[href]");

        Set<String> urls = new HashSet<>();
        for (Element element : select) {
            String href = element.attr("href");
            urls.add(BASE_URL + href);
        }
        urls.add(url);
        return urls;
    }

    private Set<String> getGenres(Document doc) {
        String genres = doc.select("p.artiststyle").text();
        genres = StringUtils.substringBetween(genres, "", "|");
        if (genres == null) {
            return Set.of();
        }
        genres = genres.trim();
        String[] split = genres.split(",");
        for(int i = 0; i < split.length; i++){
            split[i] = split[i].trim();
        }
        return Set.of(split);
    }

    private LocalDate getDate(Document doc) {

        String date = doc.select("div.event-date-location-detail").text();

        return LocalDate.parse(date.substring(3, 13), formatter);
    }


    private List<String> getBands(Document doc) {
        List<String> bands = new ArrayList<>();
        try {

            Elements select = doc.select("h3.artistname");
            for (Element band : select) {
                bands.add(band.text());
            }

        } catch (Exception ex) {
            log.warn("exception: ", ex);
        }
        return bands;
    }
}
