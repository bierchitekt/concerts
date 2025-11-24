package com.bierchitekt.concerts;

import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static java.util.Locale.ENGLISH;
import static org.mockito.Mockito.verify;

@Testcontainers
@SpringBootTest
class ConcertServiceIntegrationTest {

    @MockitoBean
    private TelegramService telegramService;

    @Autowired
    private ConcertRepository concertRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy").localizedBy(ENGLISH);


    private final LocalDate tomorrow = LocalDate.now().plusDays(1);
    private final LocalDate today = LocalDate.now();
    private final LocalDate yesterday = LocalDate.now().minusDays(1);

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(
            "postgres:latest"
    );

    @BeforeEach
    void beforeAll() {

        ConcertEntity maiden = ConcertEntity.builder()
                .date(tomorrow)
                .title("Iron Maiden")
                .genre(Set.of("Heavy Metal"))
                .supportBands("")
                .price("")
                .build();

        ConcertEntity blindGuardian = ConcertEntity.builder()
                .date(today)
                .title("Blind Guardian")
                .genre(Set.of("Power Metal"))
                .supportBands("Gamma Ray")
                .price("6.66€")
                .build();

        ConcertEntity slayer = ConcertEntity.builder()
                .date(yesterday)
                .title("Slayerrrrrrr")
                .genre(Set.of("Thrash Metal"))
                .supportBands("Suicidal Angels")
                .price("")
                .build();

        concertRepository.saveAll(List.of(maiden, blindGuardian, slayer));
    }

    @Autowired
    private ConcertService concertService;

    @Test
    void notifyNextWeekMetalConcerts() {
        concertService.notifyNextWeekMetalConcerts();

        String expectedMessage =
                "Upcoming metal concerts for next week: \n\n" +
                        "<b>Blind Guardian</b> \n" +
                        "price is 6.66€ \n" +
                        "on " + formatter.format(today) + " \n" +
                        "genre is [Power Metal] \n" +
                        "support bands are Gamma Ray\n" +
                        "<a href=\"https://bierchitekt.github.io/MunichConcertsCalendar/Blind_Guardian-24112025.ics\">add to calendar</a>\n" +
                        "playing at <a href=\"null\">null</a>\n\n" +
                        "<b>Iron Maiden</b> \n" +
                        "on " + formatter.format(tomorrow) + " \n" +
                        "genre is [Heavy Metal] \n" +
                        "<a href=\"https://bierchitekt.github.io/MunichConcertsCalendar/Iron_Maiden-25112025.ics\">add to calendar</a>\n" +
                        "playing at <a href=\"null\">null</a>\n\n";

        verify(telegramService).sendMessage("@MunichMetalConcerts", expectedMessage);
    }
}