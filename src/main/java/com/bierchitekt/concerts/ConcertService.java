package com.bierchitekt.concerts;

import com.bierchitekt.concerts.genre.GenreService;
import com.bierchitekt.concerts.persistence.ConcertEntity;
import com.bierchitekt.concerts.persistence.ConcertRepository;
import com.bierchitekt.concerts.venues.BackstageService;
import com.bierchitekt.concerts.venues.CircusKroneService;
import com.bierchitekt.concerts.venues.EventFabrikService;
import com.bierchitekt.concerts.venues.FeierwerkService;
import com.bierchitekt.concerts.venues.ImportExportService;
import com.bierchitekt.concerts.venues.KafeKultService;
import com.bierchitekt.concerts.venues.Kult9Service;
import com.bierchitekt.concerts.venues.MuffathalleService;
import com.bierchitekt.concerts.venues.OlympiaparkService;
import com.bierchitekt.concerts.venues.StringUtil;
import com.bierchitekt.concerts.venues.StromService;
import com.bierchitekt.concerts.venues.Theaterfabrik;
import com.bierchitekt.concerts.venues.TollwoodService;
import com.bierchitekt.concerts.venues.Venue;
import com.bierchitekt.concerts.venues.WinterTollwoodService;
import com.bierchitekt.concerts.venues.ZenithService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.bierchitekt.concerts.venues.Venue.BACKSTAGE;
import static com.bierchitekt.concerts.venues.Venue.CIRCUSKRONE;
import static com.bierchitekt.concerts.venues.Venue.EVENTFABRIK;
import static com.bierchitekt.concerts.venues.Venue.FEIERWERK;
import static com.bierchitekt.concerts.venues.Venue.IMPORT_EXPORT;
import static com.bierchitekt.concerts.venues.Venue.KAFE_KULT;
import static com.bierchitekt.concerts.venues.Venue.KULT9;
import static com.bierchitekt.concerts.venues.Venue.MUFFATHALLE;
import static com.bierchitekt.concerts.venues.Venue.OLYMPIAPARK;
import static com.bierchitekt.concerts.venues.Venue.STROM;
import static com.bierchitekt.concerts.venues.Venue.THEATERFABRIK;
import static com.bierchitekt.concerts.venues.Venue.TOLLWOOD;
import static com.bierchitekt.concerts.venues.Venue.WINTER_TOLLWOOD;
import static com.bierchitekt.concerts.venues.Venue.ZENITH;
import static java.util.Locale.ENGLISH;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConcertService {
    private final ConcertRepository concertRepository;
    private final TelegramService telegramService;

    @Value("${disabled.venues}")
    private final Set<Venue> disabledVenues;

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
    private final ImportExportService importExportService;
    private final TollwoodService tollwoodService;
    private final WinterTollwoodService winterTollwoodService;
    private final JsonWriter jsonWriter;

    private final ICalService icalService;
    private final ConcertMapper concertMapper;

    private final GenreService genreService;

    public static final String CALENDAR_URL = "https://bierchitekt.github.io/MunichConcertsCalendar/";

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy").localizedBy(ENGLISH);

    @Transactional
    public void notifyNewConcerts() {
        log.info("notifying for new concerts");
        List<ConcertEntity> newMetalConcerts = concertRepository.findConcertsByGenreAndNotNotifiedOrderByDate("metal");
        List<ConcertEntity> newRockConcerts = concertRepository.findConcertsByGenreAndNotNotifiedOrderByDate("rock");
        List<ConcertEntity> newPunkConcerts = concertRepository.findConcertsByGenreAndNotNotifiedOrderByDate("punk");
        List<ConcertEntity> newHardcoreConcerts = concertRepository.findConcertsByGenreAndNotNotifiedOrderByDate("hardcore");

        notifyNewConcerts("Good news everyone! I found some new metal concerts for you\n\n", newMetalConcerts, "@MunichMetalConcerts");
        notifyNewConcerts("Good news everyone! I found some new rock concerts for you\n\n", newRockConcerts, "@MunichRockConcerts");
        notifyNewConcerts("Good news everyone! I found some new punk concerts for you\n\n", newPunkConcerts, "@MunichPunkConcerts");
        notifyNewConcerts("Good news everyone! I found some new hardcore concerts for you\n\n", newHardcoreConcerts, "@MunichHardcoreConcerts");
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

        allConcerts.addAll(getStromConcerts());
        allConcerts.addAll(getZenithConcerts());
        allConcerts.addAll(getBackstageConcerts());
        allConcerts.addAll(getImportExportConcerts());
        allConcerts.addAll(getMuffathalleConcerts());
        allConcerts.addAll(getEventfabrikConcerts());
        allConcerts.addAll(getCircusKroneConcerts());
        allConcerts.addAll(getFeierwerkConcerts());
        allConcerts.addAll(getOlympiaparkConcerts());
        allConcerts.addAll(getKult9Concerts());
        allConcerts.addAll(getTheaterfabrikConcerts());
        allConcerts.addAll(getKafeKultConcerts());
        allConcerts.addAll(getTollwoodConcerts());
        allConcerts.addAll(getWinterTollwoodConcerts());

        log.info("found {} concerts, saving now", allConcerts.size());

        for (ConcertDTO concertDTO : allConcerts) {
            if (concertDTO.date() == null) {
                log.info(concertDTO.toString());
                continue;
            }
            if (concertDTO.title().isBlank()) {
                log.error("concert title is missing for link {}", concertDTO.link());
                continue;
            }
            if (concertDTO.date().isBefore(LocalDate.now())) {
                log.debug("Not adding concert {} because it's on {} and older than today", concertDTO.title(), concertDTO.date());
                continue;
            }
            if (concertRepository.similarTitleAtSameDate(concertDTO.title(), concertDTO.date()).isEmpty()) {
                log.info("new concert found. Title: {}, date: {}, venue: {}", concertDTO.title(), concertDTO.date(), concertDTO.location());
                ConcertEntity concertEntity = concertMapper.toConcertEntity(concertDTO);
                concertEntity.setAddedAt(LocalDate.now());
                concertRepository.save(concertEntity);
            }
        }
        jsonWriter.writeJsonToDisk(getConcertDTOs());
        icalService.createICalEntries(getConcertDTOs());
        sendErrorsToTelegram();
    }

    public List<ConcertDTO> getNextWeekConcerts() {

        List<ConcertEntity> byDateAfterAndDateBeforeOrderByDate = concertRepository.findByDateAfterAndDateBeforeOrderByDate(LocalDate.now(), LocalDate.now().plusDays(8));

        return concertMapper.toConcertDto(byDateAfterAndDateBeforeOrderByDate);
    }

    public void deleteOldConcerts() {
        List<ConcertEntity> allByDateBefore = concertRepository.findAllByDateBefore(LocalDate.now());
        if (!allByDateBefore.isEmpty()) {
            log.info("deleting {} old concerts", allByDateBefore.size());
            concertRepository.deleteAll(allByDateBefore);
        }
    }

    private Collection<ConcertDTO> getCircusKroneConcerts() {
        return getNewConcerts(circusKroneService.getConcerts(), CIRCUSKRONE);
    }

    private Collection<ConcertDTO> getEventfabrikConcerts() {
        return getNewConcerts(eventFabrikService.getConcerts(), EVENTFABRIK);
    }

    private Collection<ConcertDTO> getMuffathalleConcerts() {
        return getNewConcerts(muffathalleService.getConcerts(), MUFFATHALLE);
    }

    private Collection<ConcertDTO> getTheaterfabrikConcerts() {
        return getNewConcerts(theaterfabrikService.getConcerts(), THEATERFABRIK);
    }

    private Collection<ConcertDTO> getTollwoodConcerts() {
        return getNewConcerts(tollwoodService.getConcerts(), TOLLWOOD);
    }

    private Collection<ConcertDTO> getWinterTollwoodConcerts() {
        return getNewConcerts(winterTollwoodService.getConcerts(), WINTER_TOLLWOOD);
    }

    private Collection<ConcertDTO> getOlympiaparkConcerts() {
        return getNewConcerts(olympiaparkService.getConcerts(), OLYMPIAPARK);
    }

    private Collection<ConcertDTO> getZenithConcerts() {
        return getNewConcerts(zenithService.getConcerts(), ZENITH);
    }

    private Collection<ConcertDTO> getStromConcerts() {
        return getNewConcerts(stromService.getConcerts(), STROM);
    }

    private Collection<ConcertDTO> getKafeKultConcerts() {
        return getNewConcerts(kafeKultService.getConcerts(), KAFE_KULT);
    }

    private Collection<ConcertDTO> getImportExportConcerts() {
        return getNewConcerts(importExportService.getConcerts(), IMPORT_EXPORT);
    }

    private List<ConcertDTO> getNewConcerts(List<ConcertDTO> concerts, Venue venue) {
        if (disabledVenues.contains(venue)) {
            log.info("not getting concerts because venue {} is disabled", venue);
            return new ArrayList<>();
        }
        if (concerts.isEmpty()) {
            notifyNoConcertsFoundForVenue(venue);
        }
        List<ConcertDTO> newConcerts = new ArrayList<>();
        concerts.forEach(concert -> {
            if (concertRepository.similarTitleAtSameDate(concert.title(), concert.date()).isEmpty()) {
                Set<String> genres;
                if (concert.genre() != null) {
                    genres = concert.genre();
                } else {
                    genres = genreService.getGenres(concert.title());
                }
                if (venue == ZENITH) {
                    String beginn = zenithService.getTime(concert.link());
                    String supportBands = zenithService.getSupportBands(concert.link());
                    LocalDateTime dateAndTime = LocalDateTime.of(concert.date(), LocalTime.parse(beginn));
                    newConcerts.add(new ConcertDTO(concert.title(), concert.date(), dateAndTime, concert.link(), genres, concert.location(), supportBands, LocalDate.now(), concert.price(), CALENDAR_URL + StringUtil.getICSFilename(concert)));

                } else if (venue == STROM) {
                    String beginn = stromService.getTime(concert.link());
                    LocalDateTime dateAndTime = LocalDateTime.of(concert.date(), LocalTime.parse(beginn));
                    newConcerts.add(new ConcertDTO(concert.title(), concert.date(), dateAndTime, concert.link(), genres, concert.location(), concert.supportBands(), LocalDate.now(), concert.price(), CALENDAR_URL + StringUtil.getICSFilename(concert)));

                } else if (venue == CIRCUSKRONE) {
                    LocalTime beginn = circusKroneService.getBeginn(concert.link());
                    LocalDateTime dateAndTime = LocalDateTime.of(concert.date(), beginn);
                    newConcerts.add(new ConcertDTO(concert.title(), concert.date(), dateAndTime, concert.link(), genres, concert.location(), concert.supportBands(), LocalDate.now(), concert.price(), CALENDAR_URL + StringUtil.getICSFilename(concert)));

                } else if (venue == TOLLWOOD) {
                    String price = tollwoodService.getPrice(concert.link());
                    newConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.dateAndTime(), concert.link(), genres, concert.location(), concert.supportBands(), LocalDate.now(), price, CALENDAR_URL + StringUtil.getICSFilename(concert)));
                } else if (venue == MUFFATHALLE) {
                    genres = genreService.getGenres(concert.title());
                    String price = muffathalleService.getPrice(concert.link());

                    newConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.dateAndTime(), concert.link(), genres, concert.location(), "", LocalDate.now(), price, CALENDAR_URL + StringUtil.getICSFilename(concert)));
                } else {
                    newConcerts.add(new ConcertDTO(concert.title(), concert.date(), concert.dateAndTime(), concert.link(), genres, concert.location(), concert.supportBands(), LocalDate.now(), concert.price(), CALENDAR_URL + StringUtil.getICSFilename(concert)));
                }
            }
        });

        return newConcerts;
    }

    private Collection<ConcertDTO> getKult9Concerts() {
        if (disabledVenues.contains(KULT9)) {
            return new ArrayList<>();
        }
        List<ConcertDTO> kult9Concerts = new ArrayList<>();
        List<ConcertDTO> concerts = kult9Service.getConcerts();
        if (concerts.isEmpty()) {
            notifyNoConcertsFoundForVenue(KULT9);
        }
        concerts.forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                kult9Concerts.add(new ConcertDTO(concert.title(), concert.date(), concert.dateAndTime(), concert.link(), concert.genre(), concert.location(), concert.supportBands(), LocalDate.now(), concert.price(), CALENDAR_URL + StringUtil.getICSFilename(concert)));
            }
        });

        return kult9Concerts;
    }

    private Collection<ConcertDTO> getFeierwerkConcerts() {
        if (disabledVenues.contains(FEIERWERK)) {
            return new ArrayList<>();
        }
        List<ConcertDTO> feierwerkConcerts = new ArrayList<>();
        Set<String> concertLinks = feierwerkService.getConcertLinks();
        for (String url : concertLinks) {
            if (concertRepository.findByLink(url).isEmpty()) {
                Optional<ConcertDTO> concertOptional = feierwerkService.getConcert(url);
                concertOptional.ifPresent(feierwerkConcerts::add);
            }
        }
        if (concertLinks.isEmpty()) {
            notifyNoConcertsFoundForVenue(FEIERWERK);
        }
        log.info("received {} concerts for {}}", feierwerkConcerts.size(), FEIERWERK.getName());
        return feierwerkConcerts;
    }

    public List<ConcertDTO> getBackstageConcerts() {
        if (disabledVenues.contains(BACKSTAGE)) {
            return new ArrayList<>();
        }
        List<ConcertDTO> backstageConcerts = new ArrayList<>();
        List<ConcertDTO> concerts = backstageService.getConcerts();
        concerts.forEach(concert -> {
            if (concertRepository.findByTitleAndDate(concert.title(), concert.date()).isEmpty()) {
                String supportBands = backstageService.getSupportBands(concert.link());
                String price;
                Pair<@NotNull String, @NotNull String> priceAndTime = backstageService.getPriceAndTime(concert.link());
                if (concert.title().contains("Free&easy")) {
                    price = "0 €";
                } else {
                    price = priceAndTime.getFirst();
                }
                String startTime = priceAndTime.getSecond();
                LocalDateTime concertDateAndTime = LocalDateTime.of(concert.date(), LocalTime.parse(startTime));

                ConcertDTO concertDTO = ConcertDTO.builder()
                        .title(concert.title())
                        .date(concert.date())
                        .dateAndTime(concertDateAndTime)
                        .link(concert.link())
                        .genre(concert.genre())
                        .location(concert.location())
                        .supportBands(supportBands)
                        .addedAt(LocalDate.now())
                        .price(price)
                        .calendarUri(CALENDAR_URL + StringUtil.getICSFilename(concert))
                        .build();

                backstageConcerts.add(concertDTO);
            }
        });
        if (concerts.isEmpty()) {
            notifyNoConcertsFoundForVenue(BACKSTAGE);
        }
        return backstageConcerts;
    }

    private List<ConcertDTO> getConcertDTOs() {
        List<ConcertEntity> concerts = concertRepository.findByDateAfterOrderByDate(LocalDate.now().minusDays(1));
        return concertMapper.toConcertDto(concerts);
    }

    private void notifyNoConcertsFoundForVenue(Venue venue) {
        log.error("did not get any concerts for venue: {}", venue.getName());
    }

    private void notifyNewConcerts(String message, List<ConcertEntity> newConcerts, String channelName) {
        StringBuilder stringBuilder = new StringBuilder(message);
        if (!newConcerts.isEmpty()) {
            for (ConcertEntity concert : newConcerts) {
                stringBuilder
                        .append("<b>").append(concert.getTitle()).append("</b> \n");
                if (!concert.getPrice().isEmpty()) {
                    stringBuilder
                            .append("price is ").append(concert.getPrice()).append(" \n");
                }
                stringBuilder
                        .append("on ").append(concert.getDate().format(formatter)).append(" \n")
                        .append("genre is ").append(concert.getGenre()).append(" \n");
                if (!concert.getSupportBands().isEmpty()) {
                    stringBuilder.append("support bands are ").append(concert.getSupportBands()).append("\n");
                }
                stringBuilder
                        .append("<a href=\"")
                        .append(CALENDAR_URL)
                        .append(StringUtil.getICSFilename(concertMapper.toConcertDto(concert)))
                        .append("\">")
                        .append("add to calendar")
                        .append("</a>\n");

                stringBuilder.append("playing at <a href=\"").append(concert.getLink()).append("\">").append(concert.getLocation()).append("</a>\n\n");
            }
            telegramService.sendMessage(channelName, stringBuilder.toString());
        }
        setNotified(newConcerts);
    }

    private void setNotified(List<ConcertEntity> concerts) {
        for (ConcertEntity concert : concerts) {
            concert.setNotified(true);
            concertRepository.save(concert);
        }
    }

    public void sendErrorsToTelegram() {
        List<String> errors = FunctionTriggerAppender.getErrors();
        StringBuilder stringBuilder = new StringBuilder();

        for (String error : errors) {
            stringBuilder.append(error).append("\n");
        }
        telegramService.sendMessage("@concerterrors", stringBuilder.toString());
        FunctionTriggerAppender.resetErrors();

    }

}

