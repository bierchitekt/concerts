package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bierchitekt.concerts.venues.StringUtil.capitalizeWords;
import static com.bierchitekt.concerts.venues.Venue.STROM;

@Slf4j
@Service
@RequiredArgsConstructor
public class StromService {

    private static final String URL = "https://strom-muc.de/";
    public static final String VENUE_NAME = STROM.getName();

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


                    ConcertDTO concertDTO = new ConcertDTO(title, date, null, link, null, VENUE_NAME, "", LocalDate.now(), "", "");
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

    public String getTime(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            String text = doc.select("div.gdlr-info-time.gdlr-info").first().text();
            String start = StringUtils.substringBetween(text, "Beginn: ", " Uhr");

            String regex = "\\b([01]?[0-9]|2[0-3][:.][0-5][0-9])\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            if(matcher.find()){
                start = matcher.group(1);
            }

            if (start == null) {
                start = StringUtils.substringBetween(text, " / ", " Uhr");
            }
            if (start == null) {
                start = StringUtils.substringBetween(text, "Einlass: ", " Uhr");
            }
            if (start == null) {
                log.warn("could not find time for {} url {}", VENUE_NAME, url);
                return "20:00";
            }
            if (start.length() == 2) {
                return start + ":00";
            }
            return start.replace(".", ":").substring(0, 5);
        } catch (IOException e) {
            log.warn("error getting time for strom url {} ", "link", e);
            return "";
        }
    }

}
