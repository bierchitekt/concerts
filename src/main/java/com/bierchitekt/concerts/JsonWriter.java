package com.bierchitekt.concerts;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

@Slf4j
@Component
public class JsonWriter {

    @Value("${export-json.file-name}")
    private String filename;

    public void writeJsonToDisk(List<ConcertDTO> concertDTOs) {
        String jsonString = getJsonString(concertDTOs);
        try {
            Files.writeString(
                    Paths.get(filename),
                    jsonString);
        } catch (IOException e) {
            log.error("error while writing concerts to disk", e);
        }
    }
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public String getJsonString(List<ConcertDTO> concertDTOs) {
        try {
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            objectMapper.setDateFormat(dateFormat);

            return objectMapper.writeValueAsString(concertDTOs);
        } catch (IOException e) {
            log.error("error while writing concerts to json", e);
            return "";
        }
    }
}
