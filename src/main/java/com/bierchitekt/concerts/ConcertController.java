package com.bierchitekt.concerts;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @PostMapping("/update-concerts")
    public void getConcerts() {
        concertService.getNewConcerts();
    }

    @PostMapping("/notify-new-concerts")
    public void notifyNewConcerts() {
        concertService.notifyNewConcerts();
    }

    @PostMapping("/notify-nextweek-metal-concerts")
    public void notifyNextWeekMetalConcerts() {
        concertService.notifyNextWeekMetalConcerts();
    }

    @PostMapping("/notify-nextweek-punk-concerts")
    public void notifyNextWeekPunkConcerts() {
        concertService.notifyNextWeekPunkConcerts();
    }

    @PostMapping("/notify-nextweek-rock-concerts")
    public void notifyNextWeekRockConcerts() {
        concertService.notifyNextWeekRockConcerts();
    }

    @GetMapping("/next-week-metal-concerts")
    public List<ConcertDTO> getNextWeekConcerts() {
        return concertService.getNextWeekConcerts();
    }

    @GetMapping("/new-not-yet-notified-concerts")
    public Set<ConcertDTO> getNewNotYetNotifiedConcerts() {
        return concertService.getNotYetNotifiedConcerts();
    }
}
