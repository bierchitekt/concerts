package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.bierchitekt.concerts.venues.StringUtil.capitalizeWords;

@Slf4j
@Service
@RequiredArgsConstructor
public class StromService {

    private static final String URL = "https://strom-muc.de/";
    private static final String VENUE_NAME = "Strom";

    public List<ConcertDTO> getConcerts() {
        log.info("getting {} concerts", VENUE_NAME);

        List<ConcertDTO> allConcerts = new ArrayList<>();
        try {

            Document doc = Jsoup.connect(URL).get();

            Elements scriptElements = doc.getElementsByTag("script");

            for (Element element : scriptElements) {
                if (element.data().contains("events.push")) {
                    String data = element.data();
                    String answer = data.substring(data.indexOf("(") + 1, data.indexOf(")"));

                    String type = JsonParser.parseString(answer).getAsJsonObject().get("location").getAsString();

                    if (!"KONZERT".equalsIgnoreCase(type)) {
                        continue;
                    }
                    String datetime = JsonParser.parseString(answer).getAsJsonObject().get("datetime").getAsString();

                    LocalDate date = Instant.ofEpochMilli(Long.parseLong(datetime)).atZone(ZoneId.systemDefault()).toLocalDate();


                    String title = JsonParser.parseString(answer).getAsJsonObject().get("title").getAsString();
                    title = StringEscapeUtils.unescapeHtml4(title);
                    title = capitalizeWords(title);
                    String link = JsonParser.parseString(answer).getAsJsonObject().get("permalink").getAsString();

                    ConcertDTO concertDTO = new ConcertDTO(title, date, link, null, VENUE_NAME, "", LocalDate.now(), "");
                    allConcerts.add(concertDTO);
                }

            }
        } catch (Exception ex) {
            log.warn("error getting {} concerts", VENUE_NAME, ex);
            return allConcerts;
        }
        log.info("received {} {} concerts", allConcerts.size(), VENUE_NAME);

        return allConcerts;

    }
}
