package com.bierchitekt.concerts.persistence;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Set;


@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConcertEntity implements Comparable<ConcertEntity> {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String title;
    private String supportBands;
    private LocalDate date;
    private String link;
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> genre;
    private String location;
    private boolean notified;
    private LocalDate addedAt;

    @Override
    public int compareTo(@NotNull ConcertEntity other) {
        return date.compareTo(other.date);
    }
}