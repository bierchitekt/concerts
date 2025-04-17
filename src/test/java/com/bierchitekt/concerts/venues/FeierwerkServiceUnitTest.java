package com.bierchitekt.concerts.venues;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FeierwerkServiceUnitTest {

    private final FeierwerkService feierwerkService = new FeierwerkService();

    @ParameterizedTest
    @CsvSource({
            "VVK: 25 AK: 30 Euro, 25 €",
            "VVK: 20, 20 €",
            "AK: 4 Euro |free entry for refugees, 4 €",
            "AK: 10 Euro, 10 €",
           // "AK: pay what you want (Empfehlung: 3-5 Euro), 3-5 €"
    })
    void extractPrice(String input, String expected) {
        String s = feierwerkService.extractPrice(input);
        assertThat(s).isEqualTo(expected);
    }
}