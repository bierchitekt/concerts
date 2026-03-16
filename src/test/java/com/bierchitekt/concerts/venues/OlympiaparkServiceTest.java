package com.bierchitekt.concerts.venues;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OlympiaparkServiceTest {

    private final OlympiaparkService sut = new OlympiaparkService();

    @Test
    void getGenresEmptyString() {
        String genre = "";
        Set<String> genres = sut.getGenres(genre);
        assertThat(genres).isEmpty();
    }

    @Test
    void getGenresSingleGenre() {
        String genre = "Metal";
        Set<String> expected = Set.of("Metal");
        Set<String> genres = sut.getGenres(genre);
        assertThat(genres).isEqualTo(expected);
    }

    @Test
    void getGenresCommaSeparatedString() {
        String genre = "Metal, Rock";
        Set<String> expected = Set.of("Metal", "Rock");
        Set<String> genres = sut.getGenres(genre);
        assertThat(genres).isEqualTo(expected);
    }

    @Test
    void getGenresCommaAndAmpersandSeparatedString() {
        String genre = "Metal, Rock & Techno";
        Set<String> expected = Set.of("Metal", "Rock", "Techno");
        Set<String> genres = sut.getGenres(genre);
        assertThat(genres).isEqualTo(expected);
    }
}