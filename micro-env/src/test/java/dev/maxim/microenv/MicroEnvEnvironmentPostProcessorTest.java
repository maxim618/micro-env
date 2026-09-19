package dev.maxim.microenv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MicroEnvEnvironmentPostProcessorTest {

    @TempDir
    Path tempDir;

    private final MicroEnvEnvironmentPostProcessor postProcessor =
            new MicroEnvEnvironmentPostProcessor();

    @Test
    void appliesLoadedConfigurationToEnvironment() throws IOException {
        writeManifest("secrets=.env");
        writeFile("secrets/.env", "DEMO_VALUE=from-file");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () -> postProcessor.postProcessEnvironment(environment, null));

        assertEquals("from-file", environment.getProperty("DEMO_VALUE"));
    }

    @Test
    void doesNotApplyConfigurationWhenFileIsInvalid() throws IOException {
        writeManifest("secrets=.env");
        writeFile("secrets/.env", "BROKEN");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () -> postProcessor.postProcessEnvironment(environment, null));

        assertNull(environment.getProperty("BROKEN"));
        assertNull(environment.getProperty("micro-env.poc"));
    }

    @Test
    void doesNotApplyConfigurationWhenFileIsMissing() throws IOException {
        writeManifest("secrets=.env");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () -> postProcessor.postProcessEnvironment(environment, null));

        assertNull(environment.getProperty("DEMO_VALUE"));
    }

    @Test
    void doesNothingWhenManifestIsMissing() {
        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () -> postProcessor.postProcessEnvironment(environment, null));

        assertNull(environment.getProperty("DEMO_VALUE"));
        assertNull(environment.getProperty("micro-env.poc"));
    }

    private void writeManifest(String content) throws IOException {
        Files.writeString(tempDir.resolve("micro-env.list"), content);
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path path = tempDir.resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private void withUserDir(Path directory, Runnable action) {
        String previous = System.getProperty("user.dir");
        System.setProperty("user.dir", directory.toString());
        try {
            action.run();
        } finally {
            if (previous == null) {
                System.clearProperty("user.dir");
            } else {
                System.setProperty("user.dir", previous);
            }
        }
    }
}
