package se.yrgo.libraryapp.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;


public class UtilsTest {

    @ParameterizedTest
    @CsvSource({"h4ss3, hasse", "10553, losse"})
    void validLeetspeakNumbers(String name, String expected) {
        String filteredName = Utils.cleanAndUnLeet(name);
        assertThat(filteredName).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"H4ss3, hasse", "L0553, losse"})
    void validLeetspeakCapitalization(String name, String expected) {
        String filteredName = Utils.cleanAndUnLeet(name);
        assertThat(filteredName).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"H4s s3, has se", "L 0553, l osse"})
    void validLeetspeakWhitespace(String name, String expected) {
        String filteredName = Utils.cleanAndUnLeet(name);
        assertThat(filteredName).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"12345, leas"})
    void leetSpeakOnlyNumbeurs(String name, String expected) {
        String filteredName = Utils.cleanAndUnLeet(name);
        assertThat(filteredName).isEqualTo(expected);
    }
}
