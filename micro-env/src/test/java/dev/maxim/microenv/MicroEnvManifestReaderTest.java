package dev.maxim.microenv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicroEnvManifestReaderTest {

    @TempDir
    Path tempDir;

    private final MicroEnvManifestReader reader =
            new MicroEnvManifestReader();

    @Test
    void findsManifestInApplicationDirectory() throws IOException {
        Path applicationDirectory = tempDir.resolve("application");
        Files.createDirectories(applicationDirectory);
        Files.writeString(
                applicationDirectory.resolve("micro-env.list"),
                "secrets=.env"
        );

        Optional<MicroEnvManifest> manifest =
                reader.readManifestFrom(applicationDirectory);

        assertTrue(manifest.isPresent());
        assertEquals(
                applicationDirectory.resolve("micro-env.list"),
                manifest.get().path()
        );
        assertEquals("secrets=.env", manifest.get().content());
    }

    @Test
    void findsManifestInParentDirectory() throws IOException {
        Path applicationDirectory =
                tempDir.resolve("application").resolve("target");

        Files.createDirectories(applicationDirectory);
        Path manifestPath = tempDir.resolve("micro-env.list");
        Files.writeString(manifestPath, "secrets=.env");

        Optional<MicroEnvManifest> manifest =
                reader.readManifestFrom(applicationDirectory);

        assertTrue(manifest.isPresent());
        assertEquals(manifestPath, manifest.get().path());
    }

    @Test
    void usesNearestManifestWhenMultipleParentsContainOne()
            throws IOException {

        Path applicationDirectory =
                tempDir.resolve("application").resolve("target");
        Path parentManifest =
                tempDir.resolve("application").resolve("micro-env.list");
        Path rootManifest =
                tempDir.resolve("micro-env.list");

        Files.createDirectories(applicationDirectory);
        Files.writeString(parentManifest, "source=parent");
        Files.writeString(rootManifest, "source=root");

        Optional<MicroEnvManifest> manifest =
                reader.readManifestFrom(applicationDirectory);

        assertTrue(manifest.isPresent());
        assertEquals(parentManifest, manifest.get().path());
        assertEquals("source=parent", manifest.get().content());
    }

    @Test
    void returnsEmptyWhenManifestDoesNotExist() throws IOException {
        Path applicationDirectory =
                tempDir.resolve("application").resolve("target");

        Files.createDirectories(applicationDirectory);

        assertTrue(reader.readManifestFrom(applicationDirectory).isEmpty());
    }
}
