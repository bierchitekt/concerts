package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OlympiaparkService {

    private static final String VENUE_NAME = "Olympiapark";

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        RestClient restClient = RestClient.create();
        String result = restClient.get()
                .uri("https://www.olympiapark.de/api/event-list?locale=de&phrase&eventType=konzerte&genre&startDate&endDate&sort=asc&location&limit=120&page=1")
                .retrieve()
                .body(String.class);
        List<ConcertDTO> allConcerts = new ArrayList<>();

        if (result == null) {
            return allConcerts;
        }
        JsonArray hits = JsonParser.parseString(result).getAsJsonObject()
                .get("hits").getAsJsonArray();
        for (JsonElement concert : hits) {
            JsonElement source = concert.getAsJsonObject().get("_source");
            String title = source.getAsJsonObject().get("title").getAsString().trim();
            String eventType = source.getAsJsonObject().get("eventType").getAsString();
            if (!"Konzerte".equalsIgnoreCase(eventType)) {
                continue;
            }
            String location = source.getAsJsonObject().get("locationName").getAsString();
            String link = source.getAsJsonObject().get("path").getAsString();
            JsonArray dateJson = source.getAsJsonObject().get("occursOn").getAsJsonArray();

            List<LocalDate> concertDates = new ArrayList<>();
            for (JsonElement dates : dateJson) {
                concertDates.add(LocalDate.parse(dates.getAsString(), formatter));
            }

            for (LocalDate date : concertDates) {
                allConcerts.add(new ConcertDTO(title, date, "https://www.olympiapark.de" + link, null, location, "", LocalDate.now(), ""));
            }

        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);

        return allConcerts;
    }

}
