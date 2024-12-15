package com.bierchitekt.concerts.genre;

import com.bierchitekt.concerts.venues.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

@Service
@Slf4j
public class SpotifyClient {


    @Value("${spotify.clientId}")
    @NotEmpty
    private String clientId;

    @Value("${spotify.clientSecret}")
    @NotEmpty
    private String clientSecret;

    private SpotifyResponse spotifyResponse;

    RestClient restClient = RestClient.create();


    public Set<String> getGenres(String artist) {
        try {

            if (StringUtils.isEmpty(artist)) {
                return Set.of();
            }
            String accessToken = getAccessToken();

            if (StringUtils.isEmpty(accessToken)) {
                return Set.of();
            }

            String escapedArtist = artist.replace(" ", "+");
            String result = restClient.get()
                    .uri("https://api.spotify.com/v1/search?q=" + escapedArtist + "&type=artist")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(String.class);
            if (StringUtils.isEmpty(result)) {
                return Set.of();
            }

            JsonArray asJsonArray = JsonParser.parseString(result).getAsJsonObject()
                    .get("artists").getAsJsonObject()
                    .get("items").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("genres")
                    .getAsJsonArray();

            Set<String> genres = new HashSet<>();
            for (int i = 0; i < asJsonArray.size(); i++) {
                genres.add(StringUtil.capitalizeWords(asJsonArray.get(i).getAsString()));
            }
            return genres;
        } catch (Exception ex) {
            return Set.of();
        }

    }

    private String getAccessToken() {

        if (spotifyResponse != null && spotifyResponse.expiresAt().isAfter(LocalDateTime.now())) {
            return spotifyResponse.accessToken();
        }

        String result = restClient.post()
                .uri("https://accounts.spotify.com/api/token")
                .contentType(APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret)
                .retrieve()
                .body(String.class);
        if (StringUtils.isEmpty(result)) {
            return "";
        }
        String accessToken = JsonParser.parseString(result).getAsJsonObject()
                .get("access_token").getAsString();
        spotifyResponse = new SpotifyResponse(accessToken, LocalDateTime.now().plusHours(59));
        return accessToken;
    }

}
