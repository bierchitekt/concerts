package com.bierchitekt.concerts.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ConcertRepository extends JpaRepository<ConcertEntity, String> {
    List<ConcertEntity> findAllByDateBefore(LocalDate now);

    List<ConcertEntity> findByDateAfterOrderByDate(LocalDate date);

    List<ConcertEntity> findByDateAfterAndDateBeforeOrderByDate(LocalDate from, LocalDate to);

    List<ConcertEntity> findByTitleAndDate(String title, LocalDate date);

    List<ConcertEntity> findByGenreIn(Set<String> s);

    Optional<ConcertEntity> findByLink(String url);

    @Query(value = "SELECT distinct * FROM concert_entity ce where genre::text ilike CONCAT('%', :genre, '%') and date >= now() and notified = false order by date;", nativeQuery = true)
    List<ConcertEntity> findConcertsByGenreAndNotNotifiedOrderByDate(@Param("genre") String genre);

    @Query(value = "SELECT distinct * FROM concert_entity ce where genre::text ilike CONCAT('%', :genre, '%') and date >= :from and date < :to order by date;", nativeQuery = true)
    List<ConcertEntity> findByGenreAndDateAfterAndDateBeforeOrderByDate(String genre, LocalDate from, LocalDate to);
}
