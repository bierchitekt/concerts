package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.bierchitekt.concerts.venues.Venue.OLYMPIAPARK;

@Service
@Slf4j
public class OlympiaparkService {

    public static final String VENUE_NAME = OLYMPIAPARK.getName();

    private static final int PAGE_LIMIT = 20;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final RestClient restClient = RestClient.create();
    private static final String BASE_URL = "https://www.olympiapark.de";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        List<ConcertDTO> allConcerts = new ArrayList<>();
        JsonArray hits = new JsonArray();
        try {
            int pages = getNumberOfPages();
            for (int i = 1; i <= pages; i++) {
                hits.addAll(getConcertsForPage(i));
            }
        } catch (CannotGetResultException e) {
            log.warn("cannot get data for {}: {}", VENUE_NAME, e.getMessage());
            return List.of();
        }

        for (JsonElement concert : hits) {
            JsonObject source = concert.getAsJsonObject().get("_source").getAsJsonObject();
            String title = source.get("title").getAsString().trim();
            String eventType = source.get("eventType").getAsString();
            if (!"Konzerte".equalsIgnoreCase(eventType)) {
                continue;
            }
            String location = source.get("locationName").getAsString();
            String link = source.get("path").getAsString();
            JsonArray dateJson = source.get("occursOn").getAsJsonArray();
            String startTime = source.get("start").getAsString();
            startTime = startTime.substring(11, 16);
            List<LocalDate> concertDates = new ArrayList<>();
            for (JsonElement dates : dateJson) {
                concertDates.add(LocalDate.parse(dates.getAsString(), formatter));
            }
            for (LocalDate date : concertDates) {
                LocalDateTime dateAndTime = LocalDateTime.of(date, LocalTime.parse(startTime));
                allConcerts.add(new ConcertDTO(title, date, dateAndTime, BASE_URL + link, null, location, "", LocalDate.now(), "", ""));
            }

        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);

        return allConcerts;
    }

    private int getNumberOfPages() throws CannotGetResultException {

        String result = restClient.get().uri(BASE_URL + "/api/event-list?locale=de&phrase&eventType=konzerte&genre&premium&startDate&endDate&sort=asc&location&limit=" + PAGE_LIMIT + "&page=1").retrieve().body(String.class);

        if (result == null) {
            throw new CannotGetResultException();
        }

        int totalCount = JsonParser.parseString(result).getAsJsonObject().get("totalCount").getAsInt();
        return (totalCount / 20) + 1;
    }

    private JsonArray getConcertsForPage(int i) throws CannotGetResultException {
        String result = restClient.get().uri(BASE_URL + "/api/event-list?locale=de&phrase&eventType=konzerte&genre&premium&startDate&endDate&sort=asc&location&limit=" + PAGE_LIMIT + " &page=" + i).retrieve().body(String.class);
        if (result == null) {
            throw new CannotGetResultException();
        }
        return JsonParser.parseString(result).getAsJsonObject().get("hits").getAsJsonArray();
    }

    private static class CannotGetResultException extends Exception {
    }
}
