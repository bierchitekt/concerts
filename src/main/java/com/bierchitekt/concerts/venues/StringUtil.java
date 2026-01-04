package com.bierchitekt.concerts.venues;

import com.bierchitekt.concerts.ConcertDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

    public static String capitalizeWords(String input) {
        if (input.equalsIgnoreCase("")) {
            return "";
        }
        // split the input string into an array of words
        input = input.toLowerCase().trim();
        String[] words = input.split("\\s");

        // StringBuilder to store the result
        StringBuilder result = new StringBuilder();

        // iterate through each word
        for (String word : words) {
            // capitalize the first letter, append the rest of the word, and add a space
            result.append(Character.toTitleCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }

        // convert StringBuilder to String and trim leading/trailing spaces
        return result.toString().trim();
    }

    public static String getICSFilename(ConcertDTO concertDTO) {
        return getICSFilename(concertDTO.title(), concertDTO.date());
    }

    public static String getICSFilename(String title, LocalDate date) {
        return StringEscapeUtils.escapeHtml4(title)
                .replace(" ", "_")
                .replace("/", "") + "-" +
                date.format(formatter) + ".ics";
    }
}
