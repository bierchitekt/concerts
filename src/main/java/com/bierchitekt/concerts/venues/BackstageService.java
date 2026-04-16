package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private static final String URL = "https://www.backstage.eu";
    private static final String GRAPHQL_URL = "https://backstage-strapi-temp.etvide-client.com/graphql";

    public static final String VENUE_NAME = BACKSTAGE.getName();
    public static final String EVENT_URL = "https://www.backstage.eu/events";

    public List<ConcertDTO> getConcerts() {
        try (HttpClient client = HttpClient.newHttpClient()) {

            String jsonBody = """
                    {
                      "operationName": "getUpcomingEventsQuery",
                      "variables": {
                        "publicationStatus": "PUBLISHED"
                      },
                      "query": "query getUpcomingEventsQuery($publicationStatus: PublicationStatus!) {  events(    sort: \\"StartZeit:asc\\"    pagination: {limit: -1}    status: $publicationStatus  ) {    ...EventFragment    __typename  }}fragment UploadFileFragment on UploadFile {  url  alternativeText  previewUrl  mime  blurhash  width  height  __typename}fragment EventFragment on Event {  EventId  Titel  Untertitel  Headline  Beschreibung  Kategorie  FilterId  StartZeit  EndZeit  LocationName  MainImage {    ...UploadFileFragment    __typename  }  HighlightFeedImage {    ...UploadFileFragment    __typename  }  HighlightModuleImage {    ...UploadFileFragment    __typename  }  DetailImage {    ...UploadFileFragment    __typename  }  Logo {    ...UploadFileFragment    __typename  }  Gallery {    ...UploadFileFragment    __typename  }  HighlightSection  HighlightFeed  UpcomingSection  MinPreisCents  VorverkaufAktiv  VorverkaufDatum  VideoUrl  ExternerProviderLink  ExternerProviderLink2  Genres {    GenreId    Name    __typename  }  Preise {    PriceId    Titel    PreisCents    TicketVerfuegbar    Gebuehren    __typename  }  ShopSections {    Position    Titel    Content    __typename  }  __typename}"
                    }
                    """;
            String token = getToken();
            if (token == null) {
                return List.of();
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GRAPHQL_URL))
                    .header("authorization", token)
                    .headers("content-type", "application/json")
                    .method("POST", HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            String body = client.send(request, HttpResponse.BodyHandlers.ofString()).body();

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = (ObjectNode) objectMapper.readTree(body);
            JsonNode jsonNode = node.get("data").get("events");


            objectMapper.registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            List<Event> events = objectMapper.treeToValue(jsonNode, new TypeReference<>() {
            });
            events.removeIf(event -> event.category == null);
            events.removeIf(event -> event.category.toLowerCase().contains("liveübertragung"));
            events.removeIf(event -> event.category.toLowerCase().contains("party"));
            events.removeIf(event -> event.category.toLowerCase().contains("fussball"));
            events.removeIf(event -> event.category.toLowerCase().contains("biergarten"));
            events.removeIf(event -> event.category.toLowerCase().contains("lesung"));
            events.removeIf(event -> event.category.toLowerCase().contains("pop! reloaded"));
            events.removeIf(event -> event.category.toLowerCase().contains("rollschuh"));
            events.removeIf(event -> event.category.toLowerCase().contains("caribbean vibes"));
            events.removeIf(event -> event.category.isEmpty());
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
                ConcertDTO concertDTO = new ConcertDTO(title, date, dateAndTime, link, genres, location, supportBands, LocalDate.now(), price, "");
                allConcerts.add(concertDTO);
            }
            return allConcerts;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    public static class Event {
        //@JsonProperty("event")
        @JsonProperty("event_id")
        private String eventId;

        //@JsonProperty("Titel")
        @JsonProperty("title")
        private String title;

    //    @JsonProperty("LocationName")
        @JsonProperty("location_name")
        private String locationName;

        private String headline;

        //@JsonProperty("Kategorie")
        private String category;

        @JsonProperty("min_price_cents")
        //@JsonProperty("MinPreisCents")
        private Integer priceInCents;

     //   @JsonProperty("StartZeit")
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
            if (headline == null) {
                return "";
            }
            String result = getHeadline().replace("Supports: ", "");
            result = result.replace("+ special guest: ", "");
            result = result.replace("+ Special Guest: ", "");
            return result;

        }
    }


    public String getToken() {
        try (Playwright playwright = Playwright.create()) {
            // Launch browser (set headless to false if you want to see it working)
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));

            try (Page page = browser.newPage()) {

                // Set a realistic User-Agent to avoid being flagged as a bot
                page.setExtraHTTPHeaders(java.util.Map.of(
                        "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                ));

                // Variable to store the found token
                final String[] bearerToken = {null};

                // Intercept network requests
                page.onResponse(response -> {
                    if (response.url().contains("/graphql")) {
                        // Get headers from the request associated with this response
                        String authHeader = response.request().headers().get("authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            bearerToken[0] = authHeader;
                            System.out.println("Found Token: " + bearerToken[0]);
                        }
                    }
                });

                // Navigate to the events page
                log.info("Navigating and solving Vercel checkpoint...");
                page.navigate(EVENT_URL);

                // Wait for the GraphQL request to trigger (adjust timeout as needed)
                page.waitForLoadState();

                // Give the site a few seconds to run its internal API calls
                Thread.sleep(5000);

                if (bearerToken[0] == null) {
                    log.info("Token not found. You might need to log in manually first.");
                }

                browser.close();
                return bearerToken[0];
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }


    @PostConstruct
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

}