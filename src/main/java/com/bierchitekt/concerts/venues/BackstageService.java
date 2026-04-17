package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bierchitekt.concerts.venues.Venue.BACKSTAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackstageService {

    public static final String VENUE_NAME = BACKSTAGE.getName();
    public static final String EVENT_URL = "https://www.backstage.eu/events";

    @PostConstruct
    public List<ConcertDTO> getConcerts() {
        log.info("starting getting concerts for venue {}", VENUE_NAME);
        long start = System.currentTimeMillis();
        List<ConcertDTO> allConcerts = new ArrayList<>();

        for (Event event : getEvents()) {
            String title = event.getTitle();

            LocalDate date = event.getStartTime().toLocalDate();
            LocalDateTime dateAndTime = event.getStartTime();
            String link = event.getLink();
            Set<String> genres = event.getGenres();
            String location = event.getLocationName();
            String price = event.getPrice();
            String supportBands = event.getSupportBands();
            ConcertDTO concertDTO = new ConcertDTO(title, date, dateAndTime, link, genres, location, supportBands,
                    LocalDate.now(), price, "");
            allConcerts.add(concertDTO);
        }
        log.info("found {} new concerts for venue {}, took {} ms", allConcerts.size(), VENUE_NAME, (System.currentTimeMillis() - start));
        return allConcerts;
    }

    public List<Event> getEvents() {
        try {
            Document document = Jsoup.connect(EVENT_URL).get();
            String script = document.getElementsByTag("script").getLast()
                    .toString();

            int eventIdIndex = script.indexOf("event_id");
            int shopSections = script.lastIndexOf("shop_sections");
            String allEvents = "[{\"" + script.substring(eventIdIndex, shopSections) + "shop_sections\":[]}]";
            allEvents = allEvents.replace("\\\"", "\"");

            allEvents = allEvents.replace("\\\\\"", "'");
            ObjectMapper mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            List<Event> events = mapper.readValue(allEvents, new TypeReference<>() {
            });

            events.removeIf(event -> event.category == null);
            events.removeIf(event -> event.category.toLowerCase().contains("liveübertragung"));
            events.removeIf(event -> event.category.toLowerCase().contains("party"));
            events.removeIf(event -> event.category.toLowerCase().contains("fussball"));
            events.removeIf(event -> event.category.toLowerCase().contains("biergarten"));
            events.removeIf(event -> event.category.toLowerCase().contains("lesung"));
            events.removeIf(event -> event.category.toLowerCase().contains("pop! reloaded"));
            events.removeIf(event -> event.category.toLowerCase().contains("rollschuh"));
            events.removeIf(event -> event.title.toLowerCase().contains("rollschuh"));
            events.removeIf(event -> event.category.toLowerCase().contains("caribbean vibes"));
            events.removeIf(event -> event.title.toLowerCase().contains("caribbean vibes"));
            events.removeIf(event -> event.category.isEmpty());

            return events;
        } catch (IOException e) {
            log.error("error getting backstage events", e);
            return List.of();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    public static class Event {
        @JsonProperty("event_id")
        private String eventId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("location_name")
        private String locationName;

        private String headline;

        private String category;

        @JsonProperty("min_price_cents")
        private Integer priceInCents;

        @JsonProperty("start_time")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
        private LocalDateTime startTime;

        public String getTitle() {
            return StringUtil.capitalizeWords(title);
        }

        public String getLink() {
            return "https://www.backstage.eu/event/" + eventId;
        }

        public String getLocationName() {
            return StringUtil.capitalizeWords(locationName);
        }

        public String getPrice() {
            if (priceInCents == null) {
                return null;
            } else {
                return priceInCents / 100 + " €";
            }
        }

        public String getHeadline() {
            return StringUtil.capitalizeWords(headline);
        }

        public Set<String> getGenres() {
            return Arrays.stream(category.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }

        public String getSupportBands() {
            if (headline == null || headline.isEmpty()) {
                return "";
            }

            String result = getHeadline().replace("Supports: ", "");
            result = result.replace("+ special guest: ", "");
            result = result.replace("+ Special Guest: ", "");
            return result;
        }
    }

}