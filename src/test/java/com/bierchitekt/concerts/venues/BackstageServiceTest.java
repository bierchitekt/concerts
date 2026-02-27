package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BackstageServiceIntegrationTest {

    @InjectMocks
    private BackstageService backstageService;

    @Test
    void shouldGetVenue() throws IOException {
        File file = ResourceUtils.getFile("classpath:venues/backstage.html");

        Document doc = Jsoup.parse(file, "UTF-8");
        List<ConcertDTO> concertsFromDocument = backstageService.getConcertsFromDocument(doc);
        assertThat(concertsFromDocument).hasSize(25);
        ConcertDTO firstConcert = concertsFromDocument.getFirst();
        assertThat(firstConcert.title()).isEqualTo("Monosphere + Despite Exile - Co-headliners Germany 2025 | Leider Abgesagt");
        assertThat(firstConcert.date()).isEqualTo(LocalDate.of(2025, 12, 16));
        assertThat(firstConcert.link()).isEqualTo("https://backstage.eu/monosphere-despite-exile-co-headliners-germany-2025.html");
        assertThat(firstConcert.location()).isEqualTo("Backstage Club");
    }

    @Test
    void getPriceAndTimeFromDocument() throws IOException {
        File file = ResourceUtils.getFile("classpath:venues/backstage-detail.html");

        Document doc = Jsoup.parse(file, "UTF-8");
        Pair<@NotNull String, @NotNull String> priceAndTimeFromDocument = backstageService.getPriceAndTimeFromDocument(doc);
        assertThat(priceAndTimeFromDocument.getSecond()).isEqualTo("20:00");
        assertThat(priceAndTimeFromDocument.getFirst()).isEqualTo("20,30 €");
    }
}