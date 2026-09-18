package dev.maxim.microenv;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MicroEnvConfigSourceValidatorTest {

    private final MicroEnvConfigSourceValidator validator = new MicroEnvConfigSourceValidator();
    private final MicroEnvManifestEntryReader entryReader = new MicroEnvManifestEntryReader();

    @Test
    void rejectsSameSourceDescribedByDifferentNormalizedPaths() {
        Path manifestPath = Path.of("project", "micro-env.list").toAbsolutePath().normalize();
        MicroEnvManifest manifest = new MicroEnvManifest(
                manifestPath,
                """
                secrets=.env
                ./secrets=.env
                """
        );

        List<Path> paths = entryReader.resolveEntries(manifest);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(paths)
        );

        assertEquals(
                "Duplicate configuration source: " + paths.get(0),
                exception.getMessage()
        );
    }

    @Test
    void allowsDifferentFilesInSameDirectory() {
        Path manifestPath = Path.of("project", "micro-env.list").toAbsolutePath().normalize();
        MicroEnvManifest manifest = new MicroEnvManifest(
                manifestPath,
                """
                secrets=.env
                secrets=.env.local
                """
        );

        assertDoesNotThrow(
                () -> validator.validate(entryReader.resolveEntries(manifest))
        );
    }

    @Test
    void allowsDistinctNormalizedPaths() {
        Path first = Path.of("project", "secrets", ".env").toAbsolutePath().normalize();
        Path second = Path.of("project", "other", ".env").toAbsolutePath().normalize();

        assertDoesNotThrow(
                () -> validator.validate(List.of(first, second))
        );
    }
}
