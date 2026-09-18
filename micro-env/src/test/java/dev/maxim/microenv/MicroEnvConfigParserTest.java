package dev.maxim.microenv;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MicroEnvConfigParserTest {

    private final MicroEnvConfigParser parser = new MicroEnvConfigParser();

    @Test
    void parsesDistinctKeys() {
        List<MicroEnvConfigEntry> entries = parser.parse("""
                FIRST=one
                SECOND=two
                """);

        assertEquals(
                List.of(
                        new MicroEnvConfigEntry("FIRST", "one"),
                        new MicroEnvConfigEntry("SECOND", "two")
                ),
                entries
        );
    }

    @Test
    void rejectsDuplicateKeyWithinOneFile() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("""
                        FIRST=one
                        FIRST=two
                        """)
        );

        assertEquals("Duplicate configuration key at line 2: FIRST", exception.getMessage());
    }

    @Test
    void treatsWhitespaceAroundKeyAsSameKey() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("""
                        FIRST=one
                         FIRST = two
                        """)
        );

        assertEquals("Duplicate configuration key at line 2: FIRST", exception.getMessage());
    }

    @Test
    void allowsEqualsInsideValue() {
        List<MicroEnvConfigEntry> entries = parser.parse("URL=https://example.test?a=b");

        assertEquals(
                List.of(new MicroEnvConfigEntry("URL", "https://example.test?a=b")),
                entries
        );
    }

    @Test
    void ignoresEmptyLinesAndComments() {
        List<MicroEnvConfigEntry> entries = parser.parse("""
                
                # comment
                VALUE=test
                
                """);

        assertEquals(
                List.of(new MicroEnvConfigEntry("VALUE", "test")),
                entries
        );
    }
}
