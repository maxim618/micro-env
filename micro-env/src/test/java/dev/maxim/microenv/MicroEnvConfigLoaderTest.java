package dev.maxim.microenv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MicroEnvConfigLoaderTest {

    @TempDir
    Path tempDir;

    private final MicroEnvConfigLoader loader = new MicroEnvConfigLoader();

    @Test
    void loadsAllConfigurationFilesBeforeReturningResult() throws IOException {
        Path common = writeFile("common.env", "COMMON=common-value");
        Path local = writeFile("local.env", "LOCAL=local-value");

        assertEquals(
                Map.of(
                        "COMMON", "common-value",
                        "LOCAL", "local-value"
                ),
                loader.load(List.of(common, local))
        );
    }

    @Test
    void invalidLaterFilePreventsReturningAnyConfiguration() throws IOException {
        Path common = writeFile("common.env", "COMMON=common-value");
        Path broken = writeFile("broken.env", "BROKEN");

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(List.of(common, broken))
        );
    }

    @Test
    void missingLaterFilePreventsReturningAnyConfiguration() throws IOException {
        Path common = writeFile("common.env", "COMMON=common-value");
        Path missing = tempDir.resolve("missing.env");

        assertThrows(
                IOException.class,
                () -> loader.load(List.of(common, missing))
        );
    }

    @Test
    void duplicateSourceIsRejectedBeforeReadingFiles() {
        Path source = tempDir.resolve("common.env");

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(List.of(source, source))
        );
    }

    private Path writeFile(String fileName, String content) throws IOException {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, content);
        return path;
    }
}
