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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bierchitekt.concerts.venues.Venue.TOLLWOOD;
import static java.util.Locale.GERMAN;

@Slf4j
@Service
@RequiredArgsConstructor
public class TollwoodService {

    private static final String URL = "https://www.tollwood.de/kalender-sommer-2026/";

    public static final String VENUE_NAME = TOLLWOOD.getName();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd. LLLL yyyy").localizedBy(GERMAN);


    @SuppressWarnings("java:S2142")
    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        Set<ConcertDTO> allConcerts = new HashSet<>();
        try {
            String html = getHTML(URL);
            Document doc = Jsoup.parse(html);

            Elements allEvents = doc.select("div.panel-calendar-tile");

            for (Element event : allEvents) {
                LocalDate date = LocalDate.parse(event.select("span.date").text(), formatter);

                Elements elementsForDay = event.select("tr");
                for (Element concertElement : elementsForDay) {
                    String category = concertElement.select("td.cell-category").text();


                    if (!"Musik".equals(category)) {
                        continue;
                    }
                    String title = concertElement.select("td.cell-title").text();
                    if (title.startsWith("Live-Musik") || title.startsWith("DJ Lounge") || title.startsWith("Rudelsingen") || title.startsWith("Pub-Musik")) {
                        continue;
                    }
                    Pair bands = getBands(title);
                    String link = concertElement.select("td.cell-details").select("a[href]").getFirst().attr("href");

                    String start = concertElement.select("td.cell-time").text();
                    String eintritt = concertElement.select("td.cell-admission").text();
                    String price = "";
                    if ("Eintritt frei".equals(eintritt)) {
                        price = eintritt;
                    }
                    LocalDateTime dateAndTime = LocalDateTime.of(date, LocalTime.parse(start.substring(0, 5)));
                    ConcertDTO concertDTO = new ConcertDTO(bands.title, date, dateAndTime, link, null, VENUE_NAME, bands.supportBands, LocalDate.now(), price, "");
                    allConcerts.add(concertDTO);
                }


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

            return price + " €";
        } catch (IOException | InterruptedException ex) {
            log.warn("cannot get price for {}", link, ex);
            return "";
        }
    }

    private Pair getBands(String allBands) {
        String title = allBands;
        List<String> supportBandsList = new ArrayList<>();

        if (allBands.contains("&")) {
            String[] split = allBands.split("&");
            title = split[0].trim();
            for (int i = 1; i < split.length; i++) {
                supportBandsList.add(split[i].trim());
            }
        }
        if (allBands.contains(",")) {
            String[] split = allBands.split(",");
            title = split[0].trim();
            for (int i = 1; i < split.length; i++) {

                supportBandsList.add(split[i].trim());
            }
        }
        String supportBands = String.join(", ", supportBandsList);
        return new Pair(title, supportBands);
    }

    private record Pair(String title, String supportBands) {
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
