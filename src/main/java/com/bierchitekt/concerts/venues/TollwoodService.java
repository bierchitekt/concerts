package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bierchitekt.concerts.ConcertService.EUROPE_BERLIN;
import static com.bierchitekt.concerts.venues.Venue.TOLLWOOD;
import static java.util.Locale.GERMAN;

@Slf4j
@Service
@RequiredArgsConstructor
public class TollwoodService {

    private static final String URL = "https://www.tollwood.de/veranstaltungsort/musik-arena/";

    public static final String VENUE_NAME = TOLLWOOD.getName();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").localizedBy(GERMAN);


    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        Set<ConcertDTO> allConcerts = new HashSet<>();
        try {
            String html = getHTML(URL);
            Document doc = Jsoup.parse(html);

            Elements allEvents = doc.select("div.teaser-content ");

            for (Element event : allEvents) {

                String title = StringUtil.capitalizeWords(event.select("h3.headline").text());
                LocalDate date;
                LocalDateTime dateAndTime;
                try {
                    String text = event.select("h4.subline").text();
                    date = LocalDate.parse(text.substring(0, 10), formatter);
                    dateAndTime = LocalDateTime.of(date, LocalTime.parse(text.substring(13, 18)));
                } catch (DateTimeParseException e) {
                    continue;
                }
                String link = event.select("a[href]").getFirst().attr("href");

                ConcertDTO concertDTO = new ConcertDTO(title, date, dateAndTime, link, null, VENUE_NAME, "", LocalDate.now(ZoneId.of(EUROPE_BERLIN)), "", "");
                allConcerts.add(concertDTO);
            }


            log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);

            return allConcerts.stream().toList();
        } catch (Exception ex) {
            log.warn(ex.getMessage(), ex);
            return List.of();
        }
    }

    @SuppressWarnings("java:S2142")
    public String getPrice(String link) {
        try {
            String html = getHTML(link);

            Document doc = Jsoup.parse(html);
            Elements select = doc.select("span.price");
            String price = select.text();
            price = price.replaceAll("[^0-9,]", "");

            if (price.isBlank()) {
                return "";
            }
            return price + " €";
        } catch (IOException | InterruptedException ex) {
            log.warn("cannot get price for {}", link, ex);
            return "";
        }
    }


    private String getHTML(String url) throws IOException, InterruptedException {

        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                    .header("accept-language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("cache-control", "max-age=0")
                    .header("cookie", "borlabs-cookie=%7B%22consents%22%3A%7B%22essential%22%3A%5B%22borlabs-cookie%22%5D%7D%2C%22domainPath%22%3A%22www.tollwood.de%2F%22%2C%22expires%22%3A%22Sun%2C%2021%20Dec%202025%2009%3A54%3A17%20GMT%22%2C%22uid%22%3A%22anonymous%22%2C%22version%22%3A%221%22%7D")
                    .header("if-modified-since", "Sun, 22 Jun 2025 06:08:59 GMT")
                    .header("priority", "u=0, i")
                    .header("sec-ch-ua", "\"Google Chrome\";v=\"137\", \"Chromium\";v=\"137\", \"Not/A)Brand\";v=\"24\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"Linux\"")
                    .header("sec-fetch-dest", "document")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-site", "none")
                    .header("sec-fetch-user", "?1")
                    .header("upgrade-insecure-requests", "1")
                    .header("user-agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36")
                    .GET()
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();

        }

    }
}
