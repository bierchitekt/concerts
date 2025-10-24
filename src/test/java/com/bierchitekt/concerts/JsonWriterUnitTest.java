package com.bierchitekt.concerts;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonWriterUnitTest {

    private final JsonWriter sut = new JsonWriter();

    @Test
    void getEmptyJson() {
        List<ConcertDTO> concertDTOs = new ArrayList<>();
        String jsonString = sut.getJsonString(concertDTOs);

        assertThat(jsonString).isEqualTo("[]");
    }

    @Test
    void getJson() {
        List<ConcertDTO> concertDTOs = new ArrayList<>();
        concertDTOs.add(new ConcertDTO("Gutalax", LocalDate.of(2025, 10, 24), "http://example.com", null, "Circus Krone", "", LocalDate.now(), ""));

        String jsonString = sut.getJsonString(concertDTOs);

        String expectedString = """
                [{
                "title":"Gutalax",
                "date":"2025-10-24",
                "link":"http://example.com",
                "genre":null,
                "location":"Circus Krone",
                "supportBands":"",
                "addedAt":"2025-10-24",
                "price":""}]
                """;
        assertThat(jsonString).isEqualToIgnoringNewLines(expectedString);
    }
}