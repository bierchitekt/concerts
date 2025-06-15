package com.bierchitekt.concerts;

import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @BeforeEach
    void beforeAll() {

        ConcertEntity maiden = ConcertEntity.builder()
                .date(tomorrow)
                .title("Iron Maiden")
                .genre(Set.of("Heavy Metal"))
                .supportBands("Metallica")
                .build();

        ConcertEntity blindGuardian = ConcertEntity.builder()
                .date(today)
                .title("Blind Guardian")
                .genre(Set.of("Power Metal"))
                .supportBands("Gamma Ray")
                .build();

        ConcertEntity slayer = ConcertEntity.builder()
                .date(yesterday)
                .title("Slayerrrrrrr")
                .genre(Set.of("Thrash Metal"))
                .supportBands("Suicidal Angels")
                .build();

        concertRepository.saveAll(List.of(maiden, blindGuardian, slayer));
    }

    @Autowired
    private ConcertService concertService;

   // @Test
    void notifyNextWeekMetalConcerts() {
        concertService.notifyNextWeekMetalConcerts();

        String expectedMessage =
                "Upcoming metal concerts for next week: \n\n" +
                        "<b>Blind Guardian</b> \n" +
                        "on " + formatter.format(today) + " \n" +
                        "genre is [Power Metal] \n" +
                        "support bands are Gamma Ray\n" +
                        "playing at <a href=\"null\">null</a>\n\n" +
                        "<b>Iron Maiden</b> \n" +
                        "on " + formatter.format(tomorrow) + " \n" +
                        "genre is [Heavy Metal] \n" +
                        "support bands are Metallica\n" +
                        "playing at <a href=\"null\">null</a>\n\n";

        verify(telegramService).sendMessage("@MunichMetalConcerts", expectedMessage);
    }
}