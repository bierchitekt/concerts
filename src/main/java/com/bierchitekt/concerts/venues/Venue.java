package com.bierchitekt.concerts.venues;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Venue {
    BACKSTAGE("Backstage"),
    CIRCUSKRONE("Circus Krone"),
    EVENTFABRIK("EventFabrik"),
    FEIERWERK("Feierwerk"),
    IMPORT_EXPORT("Import Export"),
    KAFE_KULT("kafekult"),
    KULT9("Kult9"),
    MUFFATHALLE("Muffathalle"),
    OLYMPIAPARK("Olympiapark"),
    STROM("Strom"),
    THEATERFABRIK("TheaterFabrik"),
    TOLLWOOD("Tollwood"),
    WINTER_TOLLWOOD("Winter Tollwood"),
    ZENITH("Zenith");


    private final String name;
}
