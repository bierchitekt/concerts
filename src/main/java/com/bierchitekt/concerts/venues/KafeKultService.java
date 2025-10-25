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

    private static final String URL = "https://www.kafekult.de/wordpress/events/";

    private static final String VENUE_NAME = "kafekult";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {

            Document doc = Jsoup.connect(URL).get();

            Elements allEvents = doc.select("article.type-tribe_events");

            for (Element concert : allEvents) {
                String link = concert.select("a[href]").getFirst().attr("href");

                Document concertDetail = Jsoup.connect(link).get();

                String title = StringUtil.capitalizeWords(concertDetail.select("title").text());
                if (title.contains("abgesagt") || title.contains("Workshops") || title.contains("jam")) {
                    continue;
                }
                title = title
                        .replace("**matinee** ", "")
                        .replace(" - Kafe Kult", "")
                        .trim();
                List<String> bands = Arrays.stream(title.split("\\+")).toList();
                String mainAct = bands.getFirst().trim();
                String supportBands = String.join(", ", bands.subList(1, bands.size()));

                LocalDate date = getDate(concertDetail);

                ConcertDTO concertDto = new ConcertDTO(mainAct, date, link, null, VENUE_NAME, supportBands.trim(), LocalDate.now(), "");
                allConcerts.add(concertDto);
            }


        } catch (Exception ex) {
            log.warn("error getting kafekult concerts", ex);
        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);
        return allConcerts;
    }

    private LocalDate getDate(Document doc) {
        Elements scriptElements = doc.getElementsByTag("script");
        for (Element script : scriptElements) {
            String type = script.attr("type");
            if (type.contentEquals("application/ld+json")) {
                String scriptData = script.data();
                if(scriptData.contains("startDate") && scriptData.contains("endDate")) {
                    String date = scriptData.substring(scriptData.indexOf("startDate") + 12, scriptData.indexOf("endDate") - 18);
                    return LocalDate.parse(date);
                }

            }
        }
        return LocalDate.now().minusDays(1);
    }
}
