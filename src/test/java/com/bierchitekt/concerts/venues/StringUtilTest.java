package com.bierchitekt.concerts.venues;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class StringUtilTest {


    @ParameterizedTest
    @CsvSource({
            "foo, foo",
            "foo bar, foo_bar",
            "foo/bar, foobar",
            "foo/bar/biz, foobarbiz",
            "foo bar biz, foo_bar_biz"
    })
    void getICSFilename(String title, String expected) {
        String actual = StringUtil.getICSFilename(title, LocalDate.of(2020, 1, 1));
        assertThat(actual).isEqualTo(expected + "-01012020.ics");
    }

    @ParameterizedTest
    @CsvSource({
            "FOO ,Foo",
            "FOO BAR ,Foo Bar"
    })
    void capitalizeWords(String title, String expected) {
        String actual = StringUtil.capitalizeWords(title);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void capitalizeWordsEmpty() {
        String actual = StringUtil.capitalizeWords("");
        assertThat(actual).isEmpty();

    }
}