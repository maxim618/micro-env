package dev.maxim.microenv;

import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void clearManifestProperty() {
        System.clearProperty(MicroEnvManifestReader.MANIFEST_PROPERTY);
    }

    @Test
    void readsExplicitManifestPath() throws IOException {
        Path manifestPath = tempDir.resolve("custom").resolve("micro-env.custom");
        Files.createDirectories(manifestPath.getParent());
        Files.writeString(manifestPath, "secrets=.env");

        System.setProperty(
                MicroEnvManifestReader.MANIFEST_PROPERTY,
                manifestPath.toString()
        );

        Optional<MicroEnvManifest> manifest =
                reader.readDefaultManifest(null);

        assertTrue(manifest.isPresent());
        assertEquals(manifestPath, manifest.get().path());
        assertEquals("secrets=.env", manifest.get().content());
    }

    @Test
    void explicitManifestTakesPrecedenceOverDefaultDiscovery()
            throws IOException {

        Path defaultManifest =
                tempDir.resolve("application").resolve("micro-env.list");
        Path explicitManifest =
                tempDir.resolve("custom").resolve("micro-env.custom");

        Files.createDirectories(defaultManifest.getParent());
        Files.createDirectories(explicitManifest.getParent());

        Files.writeString(defaultManifest, "source=default");
        Files.writeString(explicitManifest, "source=explicit");

        System.setProperty(
                MicroEnvManifestReader.MANIFEST_PROPERTY,
                explicitManifest.toString()
        );

        Optional<MicroEnvManifest> manifest =
                reader.readDefaultManifest(null);

        assertTrue(manifest.isPresent());
        assertEquals(defaultManifest, manifest.get().path());
    }

    @Test
    void returnsEmptyWhenExplicitManifestDoesNotExist() {
        Path manifestPath = tempDir.resolve("missing").resolve("micro-env.list");

        System.setProperty(
                MicroEnvManifestReader.MANIFEST_PROPERTY,
                manifestPath.toString()
        );

        Optional<MicroEnvManifest> manifest =
                reader.readDefaultManifest(null);

        assertTrue(manifest.isEmpty());
    }

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
