package com.bierchitekt.concerts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                    Path.of(filename),
                    jsonString);
        } catch (IOException e) {
            log.error("error while writing concerts to disk", e);
        }
    }

    public String getJsonString(List<ConcertDTO> concertDTOs) {
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.writeValueAsString(concertDTOs);
    }
}
