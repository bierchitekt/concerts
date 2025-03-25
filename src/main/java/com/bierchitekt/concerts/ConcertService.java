package com.bierchitekt.concerts;

import com.bierchitekt.concerts.genre.GenreService;
import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import com.bierchitekt.concerts.venues.BackstageService;
import com.bierchitekt.concerts.venues.CircusKroneService;
import com.bierchitekt.concerts.venues.EventFabrikService;
import com.bierchitekt.concerts.venues.FeierwerkService;
import com.bierchitekt.concerts.venues.KafeKultService;
import com.bierchitekt.concerts.venues.Kult9Service;
import com.bierchitekt.concerts.venues.MuffathalleService;
import com.bierchitekt.concerts.venues.OlympiaparkService;
import com.bierchitekt.concerts.venues.StromService;
import com.bierchitekt.concerts.venues.Theaterfabrik;
import com.bierchitekt.concerts.venues.ZenithService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Locale.ENGLISH;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final TelegramService telegramService;

    private final BackstageService backstageService;
    private final ZenithService zenithService;
    private final StromService stromService;
    private final MuffathalleService muffathalleService;
    private final FeierwerkService feierwerkService;
    private final OlympiaparkService olympiaparkService;
    private final Theaterfabrik theaterfabrikService;
    private final Kult9Service kult9Service;
    private final EventFabrikService eventFabrikService;
    private final CircusKroneService circusKroneService;
    private final KafeKultService kafeKultService;

    private final ConcertMapper concertMapper;

    private final GenreService genreService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy").localizedBy(ENGLISH);

    @Transactional
    public void notifyNewConcerts() {
        log.info("notifying for new concerts");
        List<ConcertEntity> newMetalConcerts = concertRepository.findConcertsByGenreAndNotNotifiedOrderByDate("metal");
        List<ConcertEntity> newRockConcerts = concertRepository.findConcertsByGenreAndNotNotifiedOrderByDate("rock");
        List<ConcertEntity> newPunkConcerts = concertRepository.findConcertsByGenreAndNotNotifiedOrderByDate("punk");

        notifyNewConcerts("Good news everyone! I found some new metal concerts for you\n\n", newMetalConcerts, "@MunichMetalConcerts");
        notifyNewConcerts("Good news everyone! I found some new rock concerts for you\n\n", newRockConcerts, "@MunichRockConcerts");
        notifyNewConcerts("Good news everyone! I found some new punk concerts for you\n\n", newPunkConcerts, "@MunichPunkConcerts");
    }

    private void setNotified(List<ConcertEntity> concerts) {
        for (ConcertEntity concert : concerts) {
            concert.setNotified(true);
            concertRepository.save(concert);
        }
    }


    public void notifyNextWeekConcerts() {
        notifyNextWeekMetalConcerts();
        notifyNextWeekRockConcerts();
        notifyNextWeekPunkConcerts();
    }


    public void notifyNextWeekMetalConcerts() {
        notifyNextWeekConcerts("metal", "@MunichMetalConcerts");
    }

    public void notifyNextWeekRockConcerts() {
        notifyNextWeekConcerts("rock", "@MunichRockConcerts");
    }

    public void notifyNextWeekPunkConcerts() {
        notifyNextWeekConcerts("punk", "@MunichPunkConcerts");
    }

    private void notifyNextWeekConcerts(String genreName, String channelName) {

        List<ConcertEntity> concerts = concertRepository.findByGenreAndDateAfterAndDateBeforeOrderByDate(genreName,
                LocalDate.now(), LocalDate.now().plusDays(8));

        notifyNewConcerts("Upcoming " + genreName + " concerts for next week: \n\n", concerts, channelName);
    }

    public void getNewConcerts() {
        log.info("starting getting new concerts");

        List<ConcertDTO> allConcerts = new ArrayList<>();
        allConcerts.addAll(getEventfabrikConcerts());
        allConcerts.addAll(getCircusKroneConcerts());
        allConcerts.addAll(getFeierwerkConcerts());
        allConcerts.addAll(getBackstageConcerts());
        allConcerts.addAll(getOlympiaparkConcerts());
        allConcerts.addAll(getKult9Concerts());
        allConcerts.addAll(getZenithConcerts());
        allConcerts.addAll(getTheaterfabrikConcerts());
        allConcerts.addAll(getStromConcerts());
        allConcerts.addAll(getMuffathalleConcerts());
        allConcerts.addAll(getKafeKultConcerts());

        log.info("found {} concerts, saving now", allConcerts.size());
        for (ConcertDTO concertDTO : allConcerts) {
            if (concertDTO.date().isBefore(LocalDate.now())) {
                log.info("Not adding concert {} because it's on {} and older than today", concertDTO.title(), concertDTO.date());
                continue;
            }
            if (concertRepository.findByTitleAndDate(concertDTO.title(), concertDTO.date()).isEmpty()) {
                if (!concertRepository.similarTitleAtSameDate(concertDTO.title(), concertDTO.date()).isEmpty()) {
                    log.warn("entry exists {}", concertDTO.title());
                }
                log.info("new concert found. Title: {}", concertDTO.title());
                ConcertEntity concertEntity = concertMapper.toConcertEntity(concertDTO);
                concertEntity.setAddedAt(LocalDate.now());
                concertRepository.save(concertEntity);
            }
        }
        generateJSON();
    }

    public List<ConcertDTO> getNextWeekConcerts() {

        List<ConcertEntity> byDateAfterAndDateBeforeOrderByDate = concertRepository.findByDateAfterAndDateBeforeOrderByDate(LocalDate.now(), LocalDate.now().plusDays(8));

        return concertMapper.toConcertDto(byDateAfterAndDateBeforeOrderByDate);
    }

    public List<ConcertEntity> getConcertsWithoutGenre() {
        return concertRepository.findByGenreIn(Set.of("[]]"));
    }

    public void updateConcertGenre(String id, String genre) {
        Optional<ConcertEntity> byId = concertRepository.findById(id);
        if (byId.isEmpty()) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404), "Unable to find resource");
        } else {
            ConcertEntity concertEntity = byId.get();
            concertEntity.setGenre(Set.of(genre));
            concertRepository.save(concertEntity);
        }
    }

    public void deleteOldConcerts() {
        List<ConcertEntity> allByDateBefore = concertRepository.findAllByDateBefore(LocalDate.now());
        if (!allByDateBefore.isEmpty()) {
            log.info("deleting {} old concerts", allByDateBefore.size());
            concertRepository.deleteAll(allByDateBefore);
        }
    }

    private Collection<ConcertDTO> getCircusKroneConcerts() {
        return getNewConcerts(circusKroneService.getConcerts());
    }

    private Collection<ConcertDTO> getEventfabrikConcerts() {
        return getNewConcerts(eventFabrikService.getConcerts());
    }

    private Collection<ConcertDTO> getTheaterfabrikConcerts() {
        return getNewConcerts(theaterfabrikService.getConcerts());
    }


    private Collection<ConcertDTO> getOlympiaparkConcerts() {
        return getNewConcerts(olympiaparkService.getConcerts());
    }


    List<ConcertDTO> getZenithConcerts() {
        return getNewConcerts(zenithService.getConcerts());
    }


    List<ConcertDTO> getStromConcerts() {
        return getNewConcerts(stromService.getConcerts());
    }

    List<ConcertDTO> getMuffathalleConcerts() {
        return getNewConcerts(muffathalleService.getConcerts());
    }

    private Collection<ConcertDTO> getKafeKultConcerts() {
        return getNewConcerts(kafeKultService.getConcerts());
    }

    private List<ConcertDTO> getNewConcerts(List<ConcertDTO> concerts) {
        List<ConcertDTO> newConcerts = new ArrayList<>();
        concerts.forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                Set<String> genres = genreService.getGenres(concert.title());
                newConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), genres, concert.location(), "", LocalDate.now()));
            }
        });
        return newConcerts;
    }

    private Collection<ConcertDTO> getKult9Concerts() {
        List<ConcertDTO> kult9Concerts = new ArrayList<>();
        kult9Service.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                kult9Concerts.add(concert);
            }
        });
        return kult9Concerts;
    }

    public Collection<ConcertDTO> getFeierwerkConcerts() {

        List<ConcertDTO> feierwerkConcerts = new ArrayList<>();

        Set<String> concertLinks = feierwerkService.getConcertLinks();
        for (String url : concertLinks) {
            if (concertRepository.findByLink(url).isEmpty()) {
                Optional<ConcertDTO> concertOptional = feierwerkService.getConcert(url);
                concertOptional.ifPresent(feierwerkConcerts::add);
            }
        }
        return feierwerkConcerts;
    }

    public List<ConcertDTO> getBackstageConcerts() {
        List<ConcertDTO> backstageConcerts = new ArrayList<>();

        backstageService.getConcerts().forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                String supportBands = backstageService.getSupportBands(concert.link());
                backstageConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.link(), concert.genre(), concert.location(), supportBands, LocalDate.now()));
            }
        });
        return backstageConcerts;
    }


    private List<ConcertDTO> getConcertDTOs() {
        List<ConcertEntity> concerts = concertRepository.findByDateAfterOrderByDate(LocalDate.now().minusDays(1));
        return concertMapper.toConcertDto(concerts);
    }

    private void generateJSON() {
        List<ConcertDTO> concertDTOs = getConcertDTOs();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        try {
            objectMapper.writeValue(new File("concerts.json"), concertDTOs);
        } catch (IOException e) {
            log.error("error while writing concerts to json", e);
        }
    }


    private void notifyNewConcerts(String message, List<ConcertEntity> newConcerts, String channelName) {
        StringBuilder stringBuilder = new StringBuilder(message);
        if (!newConcerts.isEmpty()) {
            for (ConcertEntity concert : newConcerts) {
                stringBuilder
                        .append("<b>").append(concert.getTitle()).append("</b> \n")
                        .append("on ").append(concert.getDate().format(formatter)).append(" \n")
                        .append("genre is ").append(concert.getGenre()).append(" \n");
                if (!concert.getSupportBands().isEmpty()) {
                    stringBuilder.append("support bands are ").append(concert.getSupportBands()).append("\n");
                }
                stringBuilder.append("playing at <a href=\"").append(concert.getLink()).append("\">").append(concert.getLocation()).append("</a>\n\n");
            }
            telegramService.sendMessage(channelName, stringBuilder.toString());
        }
        setNotified(newConcerts);
    }

}

