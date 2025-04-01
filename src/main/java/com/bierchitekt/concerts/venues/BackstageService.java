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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackstageService {

    private static final Map<String, Integer> calendarMap = Map.ofEntries(Map.entry("Januar", 1), Map.entry("Februar", 2), Map.entry("März", 3), Map.entry("April", 4), Map.entry("Mai", 5), Map.entry("Juni", 6), Map.entry("Juli", 7), Map.entry("August", 8), Map.entry("September", 9), Map.entry("Oktober", 10), Map.entry("November", 11), Map.entry("Dezember", 12));
    private static final int ITEMS_PER_PAGE = 25;
    private static final String OVERVIEW_URL = "https://backstage.eu/veranstaltungen/live.html?product_list_limit=";

    private static final String VENUE_NAME = "Backstage";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);
        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {
            String url = OVERVIEW_URL + ITEMS_PER_PAGE;
            int totalElements = getPages(url);
            log.debug("Backstage total elements: {}", totalElements);
            int totalPages = totalElements / ITEMS_PER_PAGE + 2;
            log.debug("Backstage total pages: {}", totalPages);

            for (int i = 1; i < totalPages; ++i) {
                log.debug("Backstage getting page {} of {}", i, totalPages);
                url = OVERVIEW_URL + ITEMS_PER_PAGE + "&p=" + i;
                List<ConcertDTO> concerts = getConcerts(url);
                allConcerts.addAll(concerts);

            }

            log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);
            return allConcerts;
        } catch (Exception ex) {
            log.error("exception: ", ex);
            return allConcerts;
        }
    }

    public String getSupportBands(String url) {
        String supportBands = "";
        try {
            Document doc = Jsoup.connect(url).get();

            Elements select = doc.select("h5");
            if (!select.isEmpty()) {
                String text = doc.select("h5").getFirst().text();
                supportBands = StringUtils.substringAfter(text, "+ Support: ");
                if (supportBands.equalsIgnoreCase("")) {
                    supportBands = StringUtils.substringAfter(text, "Supports: ");
                }
                if (supportBands.equalsIgnoreCase("")) {
                    supportBands = StringUtils.substringAfter(text, "Special Guest: ");
                }
                if (supportBands.equalsIgnoreCase("")) {
                    supportBands = StringUtils.substringAfter(text, "special guest: ");
                }
                if (supportBands.equalsIgnoreCase("")) {
                    supportBands = StringUtils.substringAfter(text, "special guests ");
                }
                if (supportBands.equalsIgnoreCase("")) {
                    supportBands = StringUtils.substringAfter(text, "& Special Guest: ");
                }
                if (supportBands.equalsIgnoreCase("")) {
                    supportBands = StringUtils.substringAfter(text, "Special Guests: ");
                }

                if (supportBands.equalsIgnoreCase("")) {
                    supportBands = text;
                }
                if (supportBands.contains("presented by") || supportBands.contains("Veranstalter") || supportBands.contains("Eintritt Frei!")
                ) {
                    return "";
                }
            }
            return StringUtil.capitalizeWords(supportBands);
        } catch (IOException e) {
            return "";
        }
    }

    @SuppressWarnings("java:S1192")
    private List<ConcertDTO> getConcerts(String url) throws IOException {
        List<ConcertDTO> concerts = new ArrayList<>();

        Document doc = Jsoup.connect(url).get();
        Elements allEvents = doc.select("div.product.details.product-item-details");

        for (Element concert : allEvents) {
            Elements detail = concert.select("a.product-item-link");
            String title = detail.text().trim();
            if (title.isEmpty()) {
                continue;
            }

            title = StringUtil.capitalizeWords(title);
            String link = detail.select("a[href]").getFirst().attr("href");
            String day = concert.select("span.day").first().text().replace(".", "");
            String month = concert.select("span.month").first().text();
            String year = concert.select("span.year").first().text();

            LocalDate date = LocalDate.of(Integer.parseInt(year), calendarMap.get(month), Integer.parseInt(day));

            String location = concert.select("strong.eventlocation").text();
            location = StringUtil.capitalizeWords(location);
            String genre = concert.select("div.product-item-description").text().trim().replace("Learn More", "");
            String[] split = genre.split(",");
            Set<String> allGenres = new HashSet<>();

            for (String genres : split) {
                allGenres.add(genres.trim());
            }

            ConcertDTO concertDto = new ConcertDTO(title, date, link, allGenres, location, "", LocalDate.now());
            concerts.add(concertDto);
        }

        return concerts;
    }


    private int getPages(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            String pages = document.select("span.toolbar-number").get(2).text();
            return Integer.parseInt(pages);
        } catch (IOException ex) {
            return 0;
        }
    }
}
