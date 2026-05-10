package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class StuStaCulumService {

    private static final String URL = "https://www.stustaculum.de/api/v1/pages/public/programm/artists";

    private static final String VENUE_NAME = Venue.STUSTACULUM.getName();

    public List<ConcertDTO> getConcerts() {

        log.info("starting getting concerts for {}", VENUE_NAME);
        List<ConcertDTO> allConcerts = new ArrayList<>();
        RestClient restClient = RestClient.create();

        String result = restClient.get()
                .uri(URL)
                .retrieve()
                .body(String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(result);

        JsonNode jsonNode1 = root.get("page").get("sections").get(0).get("type_data").get("tiles");
        for (JsonNode node : jsonNode1) {
            if (!node.get("gig_type").asString().equals("Musik")) {
                continue;
            }
            String title = node.get("title").get("de").asString();
            String genre = node.get("genre_custom").asString();
            JsonNode jsonNode = node.get("genre_by_selection");
            Set<String> genres = new HashSet<>();
            genres.add(genre);
            if (jsonNode != null) {
                ArrayNode arrayNode = jsonNode.asArray();
                genres = StreamSupport.stream(arrayNode.spliterator(), false)
                        .map(JsonNode::asString)
                        .collect(Collectors.toSet());
            }


            Optional<LocalDateTime> dateAndTime = getStartDate(node);
            if (dateAndTime.isEmpty()) {
                continue;
            }

            String link = getLink(node);

            allConcerts.add(new ConcertDTO(title, LocalDate.from(dateAndTime.get()), dateAndTime.get(), link, genres, VENUE_NAME, "", LocalDate.now(), "", ""));

        }
        log.info("found {} concerts for {}", allConcerts.size(), VENUE_NAME);
        return allConcerts;
    }

    private Optional<LocalDateTime> getStartDate(JsonNode node) {
        JsonNode start1 = node.get("start");
        if (start1 == null) {
            return Optional.empty();
        }
        String start = start1.asString();
        if (start.equals("null") || start.isEmpty()) {
            return Optional.empty();
        }
        OffsetDateTime odt = OffsetDateTime.parse(start);
        return Optional.of(odt.toLocalDateTime());
    }

    private String getLink(JsonNode node){
        JsonNode jsonNode = node.get("media_kit_url");
        if(jsonNode != null){
            return jsonNode.asString();
        }

        String imageUrl = node.get("image_url").asString();
        return "https://www.stustaculum.de" + imageUrl;
    }
}
