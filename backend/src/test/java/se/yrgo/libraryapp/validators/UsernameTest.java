package se.yrgo.libraryapp.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UsernameTest {
    @Test
    void correctUsername() {
        assertThat(Username.validate("bosse")).isEqualTo(true);
    }

    @Test
    void incorrectUsername() {
        assertThat(Username.validate("name with space")).isEqualTo(false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"anna annson", "åke.andersson", "lis", "anna,ansson", "", " bosse"})
    void incorrectRegex(String name) {
        assertThat(Username.validate(name)).isEqualTo(false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"anna.annson", "ake-andersson", "lis@andersson", "anna_ansson"})
    void correctRegex(String name) {
        assertThat(Username.validate(name)).isEqualTo(true);
    }

    @Test
    void nullRegex() {
        assertThat(Username.validate(null)).isEqualTo(false);
    }
}
