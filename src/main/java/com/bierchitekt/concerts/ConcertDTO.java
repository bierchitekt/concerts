package com.bierchitekt.concerts;


import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record ConcertDTO(String title, LocalDate date, LocalDateTime dateAndTime, String link, Set<String> genre, String location,
                         String supportBands, LocalDate addedAt, String price) implements Serializable {
}