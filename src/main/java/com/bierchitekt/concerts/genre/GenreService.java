package com.bierchitekt.concerts.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class GenreService {
    private final SpotifyClient spotifyClient;
    private final LastFMClient lastFMClient;

    public Set<String> getGenres(String artist) {
        Set<String> genres = spotifyClient.getGenres(artist);
        if (!genres.isEmpty()) {
            return genres;
        } else {
            log.info("did not get any result from spotify for artist {}, trying lastFM", artist);
            genres = lastFMClient.getGenres(artist);
            if(genres.isEmpty()){
                log.info("did not get any result from lastFM for artist {}", artist);
            }
            return genres;
        }
    }
}
