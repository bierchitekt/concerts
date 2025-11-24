package com.bierchitekt.concerts;


import com.bierchitekt.concerts.venues.StringUtil;
import jakarta.validation.constraints.NotEmpty;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;
import net.fortuna.ical4j.validate.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class ICalService {


    @Value("${calendar-filepath}")
    @NotEmpty
    private String filepath;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

    public void createICalEntry(ConcertDTO concertDTO) {

        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone("Europe/Berlin");
        VTimeZone tz = timezone.getVTimeZone();


        String eventName = concertDTO.title();
        LocalDateTime start = concertDTO.dateAndTime();
        LocalDateTime end = concertDTO.dateAndTime().plusHours(2);
        VEvent meeting = new VEvent(start, end, eventName);

        meeting.add(tz.getTimeZoneId());

        String eventUrlString = concertDTO.link();
        Url eventUrl = null;
        try {
            eventUrl = new Url(new URI(eventUrlString));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Location l = new Location();
        l.setValue(concertDTO.location());

        Description d = new Description();
        d.setValue(concertDTO.supportBands());

        UidGenerator ug = new RandomUidGenerator(false);
        Uid uid = ug.generateUid();
        meeting.add(uid);
        meeting.add(eventUrl);
        meeting.add(d);
        meeting.add(l);

        Calendar icsCalendar = new Calendar();
        icsCalendar.add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"));
        icsCalendar.add(ImmutableVersion.VERSION_2_0);

        icsCalendar.add(meeting);

        try {
            saveCalendar(icsCalendar, filepath
                    + StringUtil.getICSFilename(concertDTO));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveCalendar(Calendar calendar, String filePath) throws IOException, ValidationException {
        try (FileOutputStream fout = new FileOutputStream(filePath)) {
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, fout);
        }
    }

    public void createICalEntries(List<ConcertDTO> concertDTOs) {
        for(ConcertDTO concertDTO : concertDTOs) {
            createICalEntry(concertDTO);
        }
    }
}
