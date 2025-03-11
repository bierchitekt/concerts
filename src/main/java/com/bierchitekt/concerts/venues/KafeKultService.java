package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class KafeKultService {


    private static final String URL = "https://www.kafekult.de/wordpress/";

    private static final String VENUE_NAME = "kafekult";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {

            Document doc = Jsoup.connect(URL).get();

            Elements allEvents = doc.select("div.ai1ec-event-title");
            for (Element concert : allEvents) {
                String link = concert.select("a[href]").getFirst().attr("href");

                Document concertDetail = Jsoup.connect(link).get();

                String title = StringUtil.capitalizeWords(concertDetail.select("h1.entry-title").text());
                if (title.contains("abgesagt") || title.contains("Workshops") || title.contains("jam")) {
                    continue;
                }
                List<String> bands = Arrays.stream(title.split("\\+")).toList();
                String mainAct = bands.getFirst();
                String supportBands = String.join(", ", bands.subList(1, bands.size()));

                String dateString = concertDetail.select("div.ai1ec-hidden.dt-start").text();

                LocalDate date = LocalDate.parse(dateString.substring(0, 10));

                ConcertDTO concertDto = new ConcertDTO(mainAct, date, link, null, VENUE_NAME, supportBands.trim());
                allConcerts.add(concertDto);
            }


        } catch (Exception ex) {
            log.warn("error getting kafekult concerts", ex);
        }
        return allConcerts;
    }
}
