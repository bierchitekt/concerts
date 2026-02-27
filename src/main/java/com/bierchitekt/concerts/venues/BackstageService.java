package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.bierchitekt.concerts.venues.Venue.BACKSTAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackstageService {

    private static final Map<String, Integer> calendarMap = Map.ofEntries(Map.entry("Januar", 1), Map.entry("Februar", 2), Map.entry("März", 3), Map.entry("April", 4), Map.entry("Mai", 5), Map.entry("Juni", 6), Map.entry("Juli", 7), Map.entry("August", 8), Map.entry("September", 9), Map.entry("Oktober", 10), Map.entry("November", 11), Map.entry("Dezember", 12));
    private static final int ITEMS_PER_PAGE = 25;
    private static final String OVERVIEW_URL = "https://backstage.eu/veranstaltungen/live.html?product_list_limit=";

    public static final String VENUE_NAME = BACKSTAGE.getName();


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
                List<ConcertDTO> concerts = getConcertsForUrl(url);
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
                supportBands = removeUnwantedFillerText(text);

            }
            return StringUtil.capitalizeWords(supportBands);
        } catch (Exception e) {
            log.warn("error getting support bands for backstage ", e);
            return "";
        }
    }

    private String removeUnwantedFillerText(String text) {
        String supportBands = StringUtils.substringAfter(text, "+ Support: ");
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
        if (supportBands.contains("presented by") || supportBands.contains("Veranstalter") || supportBands.contains("Eintritt Frei!")) {
            return "";
        }
        return supportBands;
    }

    @SuppressWarnings("java:S1192")
    private List<ConcertDTO> getConcertsForUrl(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        return getConcertsFromDocument(doc);
    }

    protected List<ConcertDTO> getConcertsFromDocument(Document doc) {
        Elements allEvents = doc.select("div.product.details.product-item-details");

        List<ConcertDTO> concerts = new ArrayList<>();
        for (Element concert : allEvents) {
            Elements detail = concert.select("a.product-item-link");
            String title = detail.text().trim();
            Optional<LocalDate> date = getDate(concert);

            if (title.isEmpty() || date.isEmpty()) {
                continue;
            }

            title = StringUtil.capitalizeWords(title);
            String link = detail.select("a[href]").getFirst().attr("href");

            String location = concert.select("strong.eventlocation").text();
            if (!location.toLowerCase().startsWith("backstage")) {
                continue;
            }
            location = StringUtil.capitalizeWords(location);
            String genre = concert.select("div.product-item-description").text().trim().replace("Learn More", "");
            String[] split = genre.split(",");
            Set<String> allGenres = new HashSet<>();

            for (String genres : split) {
                allGenres.add(genres.trim());
            }

            ConcertDTO concertDto = new ConcertDTO(title, date.get(), null, link, allGenres, location, "", LocalDate.now(), "", "");
            concerts.add(concertDto);
        }

        return concerts;
    }

    private Optional<LocalDate> getDate(Element concert) {
        Element dateElement = concert.select("span.day").first();
        if (dateElement == null) {
            return Optional.empty();
        }
        String day = dateElement.text().replace(".", "");
        Element firstMonth = concert.select("span.month").first();
        if (firstMonth == null) {
            return Optional.empty();
        }
        String month = firstMonth.text();
        Element firstYear = concert.select("span.year").first();
        if (firstYear == null) {
            return Optional.empty();
        }
        String year = firstYear.text();

        return Optional.of(LocalDate.of(Integer.parseInt(year), calendarMap.get(month), Integer.parseInt(day)));
    }


    private int getPages(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            String pages = document.select("span.toolbar-number").get(2).text();
            return Integer.parseInt(pages);
        } catch (Exception ex) {
            log.warn("error getting pages for backstage url {} ", url, ex);
            return 0;
        }
    }

    public Pair<@NotNull String, @NotNull String> getPriceAndTime(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return getPriceAndTimeFromDocument(doc);
        } catch (Exception e) {
            log.warn("error getting price for backstage url {} ", url, e);
            return Pair.of("", "");
        }
    }

    public Pair<@NotNull String, @NotNull String> getPriceAndTimeFromDocument(Document doc) {
        Elements select = doc.select("span.price");

        String price = select.text();

        Element first = doc.select("div.ticketshop-icon-clock-svg-white").first();
        if (first == null) {
            return Pair.of(price, "20:00");
        }
        String time = first.text();

        return Pair.of(price, time.substring(0, 5));
    }

}
