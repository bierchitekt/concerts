package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.bierchitekt.concerts.venues.Venue.BACKSTAGE;

@Slf4j
@Service
public class BackstageService {

    public static final String VENUE_NAME = BACKSTAGE.getName();
    public static final String EVENT_URL = "https://www.backstage.eu/events";
    private final RestClient restClient;

    // Die Basis-URL und die statischen API-Keys können auch in die application.properties
    // ausgelagert und per @Value injected werden.
    private static final String BASE_URL = "https://vhhdjliwckyzbqtjrjpp.supabase.co";
    private static final String API_KEY = "sb_publishable__7zXOMfMEPpplHogPxLazQ_iaXgzJfS";

    public BackstageService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(BASE_URL).build();
    }

    public String fetchUpcomingEvents() {
        EventRequestBody requestBody = new EventRequestBody(1000, 0);

        // Der URI-Pfad inklusive Query-Parametern
        String uriPath = "/rest/v1/rpc/get_upcoming_events" +
                "?start_time=gte.2026-06-18T22:16:34.388Z" +
                "&order=start_time.asc" +
                "&offset=0" +
                "&limit=1000";

        ResponseEntity<String> response = restClient.post()
                .uri(uriPath)
                .contentType(MediaType.APPLICATION_JSON)
                .header("apikey", API_KEY)
                .header("authorization", "Bearer " + API_KEY)
                .header("content-profile", "shop_cms")
                .header("origin", "https://www.backstage.eu")
                .header("referer", "https://www.backstage.eu/")
                .header("x-client-info", "supabase-js-web/2.105.3")
                // User-Agent simulieren, falls die API einen Bot-Schutz hat
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36")
                .body(requestBody)
                .retrieve()
                .toEntity(String.class); // Gibt das Ergebnis als JSON-String zurück

        return response.getBody();
    }

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
            String s = fetchUpcomingEvents();
            ObjectMapper mapper = new JsonMapper();
            List<Event> events = mapper.readValue(s, new TypeReference<>() {
            });

            events.removeIf(event -> event.genres == null);
            events.removeIf(event -> event.getGenres().contains("liveübertragung"));
            events.removeIf(event -> event.getGenres().contains("party"));
            events.removeIf(event -> event.getGenres().contains("feiern"));
            events.removeIf(event -> event.getGenres().contains("fussball"));
            events.removeIf(event -> event.getGenres().contains("biergarten"));
            events.removeIf(event -> event.getGenres().contains("lesung"));
            events.removeIf(event -> event.getGenres().contains("pop! reloaded"));
            events.removeIf(event -> event.getGenres().contains("rollschuh"));
            events.removeIf(event -> event.title.toLowerCase().contains("rollschuh"));
            events.removeIf(event -> event.getGenres().contains("caribbean vibes"));
            events.removeIf(event -> event.title.toLowerCase().contains("caribbean vibes"));
            events.removeIf(event -> event.genres.isEmpty());

            return events;
        } catch (Exception e) {
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

        private Set<Genres> genres;

        @JsonProperty("min_price_cents")
        private Integer priceInCents;

        @JsonProperty("start_time") //  "start_time" : "2026-05-13T18:00:00+00:00",
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        private LocalDateTime startTime;

        public String getTitle() {
            if (isFreeAndEasy() && title.contains("+")) {
                int index = title.indexOf("+");

                return StringUtil.capitalizeWords(title.substring(0, index - 1));
            }
            return StringUtil.capitalizeWords(title);
        }

        public boolean isFreeAndEasy() {
            return genres.equals("free & easy");
        }

        public String getLink() {
            return "https://www.backstage.eu/event/" + eventId;
        }

        public String getLocationName() {
            return StringUtil.capitalizeWords(locationName);
        }

        public String getPrice() {
            if (priceInCents == null) {
                return "";
            }
            if (isFreeAndEasy()) {
                return "0 € free & easy";
            }
            return priceInCents / 100 + " €";

        }

        public String getHeadline() {
            return StringUtil.capitalizeWords(headline);
        }

        public Set<String> getGenres() {
            if (isFreeAndEasy()) {
                return Set.of();
            }

            Set<String> genresNames = new HashSet<>();
            for (Genres genre : genres) {
                genresNames.add(genre.name);
            }
            return genresNames;
        }

        public String getSupportBands() {
            if (isFreeAndEasy() && title.contains("+")) {
                int index = title.indexOf("+");
                String afterPlus = title.substring(index + 1);

                // Replace all remaining '+' signs with ','
                return StringUtil.capitalizeWords(afterPlus.replace("+", ",")).replace(" ,", ",");
            }

            if (headline == null || headline.isEmpty()) {
                return "";
            }

            String result = getHeadline().replace("Supports: ", "");
            result = result.replace("+ special guest: ", "");
            result = result.replace("+ Special Guest: ", "");
            return result;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        private record Genres(String name) {
        }
    }

}