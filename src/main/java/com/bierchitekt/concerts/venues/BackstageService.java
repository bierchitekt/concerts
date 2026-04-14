package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
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
                    genreTextLowerCase.contains("caribbean vibes")) {
                return Optional.empty();
            }
            genres.add(genreText);
        }

        String title = StringUtil.capitalizeWords(select.text());
        if (title.toLowerCase().contains("rollschuh") || title.toLowerCase().contains("caribbean vibes")||title.toLowerCase().contains("party")) {
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

        ConcertDTO concertDTO = new ConcertDTO(title, date, dateAndTime, link, genres, location, "", LocalDate.now(), price, "");
        return Optional.of(concertDTO);
    }

    public List<Event> getEvents() {
        try {
            Document document = Jsoup.connect(OVERVIEW_URL).get();
            String script = document.getElementsByTag("script").getLast()
                    .toString();

            int eventIdIndex = script.indexOf("event_id");
            int shopSections = script.lastIndexOf("shop_sections");
            String allEvents = "[{\"" + script.substring(eventIdIndex, shopSections) + "shop_sections\":[]}]";
            allEvents = allEvents.replace("\\\"", "\"");

            allEvents = allEvents.replace("\\\\\"", "'");
            ObjectMapper mapper = new ObjectMapper();


            List<Event> events = mapper.readValue(allEvents, new TypeReference<List<Event>>() {
            });

            events.removeIf(event -> event.category.toLowerCase().contains("liveübertragung"));
            events.removeIf(event -> event.category.toLowerCase().contains("party"));
            events.removeIf(event -> event.category.toLowerCase().contains("fussball"));
            events.removeIf(event -> event.category.toLowerCase().contains("biergarten"));
            events.removeIf(event -> event.category.toLowerCase().contains("lesung"));
            events.removeIf(event -> event.category.toLowerCase().contains("pop! reloaded"));
            events.removeIf(event -> event.category.toLowerCase().contains("rollschuh"));
            events.removeIf(event -> event.category.toLowerCase().contains("caribbean vibes"));
            events.removeIf(event -> event.category.isEmpty());

            return events;
        } catch (IOException e) {
            log.error("error getting backstage events", e);
            return List.of();
        }
    }

    public String getSupportBands(List<Event> events, String eventId) {

        Optional<Event> matchingEvent = events.stream()
                .filter(event -> eventId.equals(event.eventId))
                .findFirst();
        return matchingEvent.map(event -> event.headline.replace("Supports: ", "")).orElse("");
    }

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Event {
        @JsonProperty("event_id")
        private String eventId;

        private String title;

        @JsonProperty("location_name")
        private String locationName;

        private String headline;
        private String category;

        public String getTitle() {
            return StringUtil.capitalizeWords(title);
        }
        public String getLocationName() {
            return StringUtil.capitalizeWords(locationName);
        }

        /*
        private String description;
        private String subtitle;

        @JsonProperty("filter_id")
        private String filterId;

        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("end_time")
        private String endTime;


        @JsonProperty("highlight_feed_image")
        private String highlightFeedImage;

        @JsonProperty("highlight_module_image")
        private String highlightModuleImage;

        @JsonProperty("detail_image")
        private String detailImage;

        private String logo;
        private List<Object> gallery; // Use a specific type if you know the gallery object structure

        @JsonProperty("highlight_section")
        private boolean highlightSection;

        @JsonProperty("highlight_feed")
        private boolean highlightFeed;

        @JsonProperty("upcoming_section")
        private boolean upcomingSection;

        @JsonProperty("min_price_cents")
        private int minPriceCents;

        @JsonProperty("presale_active")
        private boolean presaleActive;

        @JsonProperty("presale_date")
        private String presaleDate;

        @JsonProperty("video_url")
        private String videoUrl;

        @JsonProperty("external_provider_link")
        private String externalProviderLink;

        @JsonProperty("external_provider_link_2")
        private String externalProviderLink2;

        private List<String> genres;
        private List<Object> prices;

        @JsonProperty("shop_sections")
        private List<Object> shopSections;
*/
    }
}
