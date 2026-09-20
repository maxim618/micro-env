package dev.maxim.microenv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicroEnvEnvironmentPostProcessorTest {

    @TempDir
    Path tempDir;

    private final MicroEnvEnvironmentPostProcessor postProcessor =
            new MicroEnvEnvironmentPostProcessor();

    @Test
    void appliesConfigurationFromSingleFile() throws IOException {
        writeManifest("secrets=.env");
        writeFile("secrets/.env", "DEMO_VALUE=from-file");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        assertEquals("from-file", environment.getProperty("DEMO_VALUE"));
    }

    @Test
    void createsSeparatePropertySourceForEachFile() throws IOException {
        writeManifest("""
                common=.env
                local=.env
                """);

        writeFile("common/.env", "DEMO_VALUE=from-common");
        writeFile("local/.env", "DEMO_VALUE=from-local");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        List<String> microEnvSources = environment.getPropertySources()
                .stream()
                .map(PropertySource::getName)
                .filter(name -> name.startsWith("micro-env:"))
                .toList();

        assertEquals(2, microEnvSources.size());
    }

    @Test
    void laterManifestFileHasHigherPrecedence() throws IOException {
        writeManifest("""
                common=.env
                local=.env
                """);

        writeFile("common/.env", "DEMO_VALUE=from-common");
        writeFile("local/.env", "DEMO_VALUE=from-local");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        assertEquals("from-local", environment.getProperty("DEMO_VALUE"));
    }

    @Test
    void propertySourcesFollowManifestPrecedenceOrder() throws IOException {
        writeManifest("""
                common=.env
                local=.env
                """);

        writeFile("common/.env", "DEMO_VALUE=from-common");
        writeFile("local/.env", "DEMO_VALUE=from-local");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        List<PropertySource<?>> microEnvSources = environment.getPropertySources()
                .stream()
                .filter(source -> source.getName().startsWith("micro-env:"))
                .toList();

        assertEquals(2, microEnvSources.size());

        assertEquals(
                "from-local",
                microEnvSources.get(0).getProperty("DEMO_VALUE")
        );

        assertEquals(
                "from-common",
                microEnvSources.get(1).getProperty("DEMO_VALUE")
        );
    }

    @Test
    void microEnvIsBelowSystemEnvironment() throws IOException {
        writeManifest("secrets=.env");
        writeFile("secrets/.env", "DEMO_VALUE=from-micro-env");

        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().replace(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MapPropertySource(
                        StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        java.util.Map.of("DEMO_VALUE", "from-system-environment")
                )
        );

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        assertEquals("from-system-environment",
                environment.getProperty("DEMO_VALUE"));
    }

    @Test
    void microEnvIsAboveLowerPriorityPropertySource() throws IOException {
        writeManifest("secrets=.env");
        writeFile("secrets/.env", "DEMO_VALUE=from-micro-env");

        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(
                new MapPropertySource(
                        "lower-priority",
                        java.util.Map.of(
                                "DEMO_VALUE",
                                "from-lower-priority"
                        )
                )
        );

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        assertEquals("from-micro-env",
                environment.getProperty("DEMO_VALUE"));
    }

    @Test
    void doesNotApplyConfigurationWhenFileIsInvalid() throws IOException {
        writeManifest("secrets=.env");
        writeFile("secrets/.env", "BROKEN");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        assertNull(environment.getProperty("BROKEN"));
        assertFalse(hasMicroEnvPropertySource(environment));
    }

    @Test
    void doesNotApplyConfigurationWhenFileIsMissing() throws IOException {
        writeManifest("secrets=.env");

        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        assertNull(environment.getProperty("DEMO_VALUE"));
        assertFalse(hasMicroEnvPropertySource(environment));
    }

    @Test
    void doesNothingWhenManifestIsMissing() {
        StandardEnvironment environment = new StandardEnvironment();

        withUserDir(tempDir, () ->
                postProcessor.postProcessEnvironment(environment, null));

        assertNull(environment.getProperty("DEMO_VALUE"));
        assertFalse(hasMicroEnvPropertySource(environment));
    }

    private boolean hasMicroEnvPropertySource(
            StandardEnvironment environment) {

        return environment.getPropertySources()
                .stream()
                .anyMatch(source -> source.getName().startsWith("micro-env:"));
    }

    private void writeManifest(String content) throws IOException {
        Files.writeString(tempDir.resolve("micro-env.list"), content);
    }

    private void writeFile(String relativePath, String content)
            throws IOException {

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
