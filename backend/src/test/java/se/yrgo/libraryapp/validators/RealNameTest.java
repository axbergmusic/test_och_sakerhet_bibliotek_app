package se.yrgo.libraryapp.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

public class RealNameTest {

    @ParameterizedTest
    @ValueSource(strings = {"hasse.fanskap", "hasse-fanskap", "hasse_fanskap"})
    void badWords(String name) {
        assertThat(RealName.validate(name)).isEqualTo(false);
    }
}
