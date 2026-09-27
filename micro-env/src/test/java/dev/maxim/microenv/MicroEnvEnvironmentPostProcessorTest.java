package dev.maxim.microenv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.SystemEnvironmentPropertySource;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class MicroEnvEnvironmentPostProcessorTest {

    @TempDir
    Path tempDir;

    private MicroEnvEnvironmentPostProcessor postProcessor;

    @BeforeEach
    void setUp() {
        MicroEnvManifestReader manifestReader =
                new MicroEnvManifestReader() {
                    @Override
                    public Optional<MicroEnvManifest> readDefaultManifest(
                            SpringApplication application) {
                        return readManifestFrom(tempDir);
                    }
                };

        postProcessor = new MicroEnvEnvironmentPostProcessor(
                manifestReader,
                new MicroEnvManifestEntryReader(),
                new MicroEnvConfigLoader()
        );
    }

    @Test
    void appliesConfigurationFromSingleFile() throws IOException {
        writeManifest("secrets=.env");
        writeFile("secrets/.env", "DEMO_VALUE=from-file");

        StandardEnvironment environment = new StandardEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        assertEquals("from-file", environment.getProperty("DEMO_VALUE"));
    }

    @Test
    void resolvesEnvironmentStyleKeysForSpringBinding() throws IOException {
        writeManifest("secrets=.secret");
        writeFile("secrets/.secret", """
                DEMO_PORT=8081
                DEMO_ENABLED=true
                """);

        StandardEnvironment environment = new StandardEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        PropertySource<?> microEnvSource = environment.getPropertySources()
                .stream()
                .filter(source -> source.getName().startsWith("micro-env:"))
                .findFirst()
                .orElseThrow();

        assertEquals("8081", microEnvSource.getProperty("demo.port"));
        assertEquals("true", microEnvSource.getProperty("demo.enabled"));
    }

    @Test
    void bindsEnvironmentStyleKeysThroughSpringBinder() throws IOException {
        writeManifest("secrets=.secret");
        writeFile("secrets/.secret", """
            DEMO_PORT=8081
            DEMO_ENABLED=true
            """);

        StandardEnvironment environment = new StandardEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        Binder binder = Binder.get(environment);

        assertEquals(
                8081,
                binder.bind("demo.port", Bindable.of(Integer.class))
                        .orElseThrow(() -> new IllegalStateException("demo.port is not bound"))
        );

        assertEquals(
                true,
                binder.bind("demo.enabled", Bindable.of(Boolean.class))
                        .orElseThrow(() -> new IllegalStateException("demo.enabled is not bound"))
        );
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

        postProcessor.postProcessEnvironment(environment, null);

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

        postProcessor.postProcessEnvironment(environment, null);

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

        postProcessor.postProcessEnvironment(environment, null);

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
                        java.util.Map.of(
                                "DEMO_VALUE",
                                "from-system-environment"
                        )
                )
        );

        postProcessor.postProcessEnvironment(environment, null);

        assertEquals(
                "from-system-environment",
                environment.getProperty("DEMO_VALUE")
        );
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

        postProcessor.postProcessEnvironment(environment, null);

        assertEquals(
                "from-micro-env",
                environment.getProperty("DEMO_VALUE")
        );
    }

    @Test
    void doesNotApplyConfigurationWhenFileIsInvalid() throws IOException {
        writeManifest("secrets=.env");
        writeFile("secrets/.env", "BROKEN");

        StandardEnvironment environment = new StandardEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        assertNull(environment.getProperty("BROKEN"));
        assertFalse(hasMicroEnvPropertySource(environment));
    }

    @Test
    void doesNotApplyConfigurationWhenFileIsMissing() throws IOException {
        writeManifest("secrets=.env");

        StandardEnvironment environment = new StandardEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        assertNull(environment.getProperty("DEMO_VALUE"));
        assertFalse(hasMicroEnvPropertySource(environment));
    }

    @Test
    void doesNothingWhenManifestIsMissing() {
        StandardEnvironment environment = new StandardEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        assertNull(environment.getProperty("DEMO_VALUE"));
        assertFalse(hasMicroEnvPropertySource(environment));
    }
    @Test
    void preservesOriginalEnvironmentStyleKey() throws IOException {
        writeManifest("secrets=.secret");
        writeFile("secrets/.secret", """
            DEMO_PORT=8081
            """);

        StandardEnvironment environment = new StandardEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        PropertySource<?> microEnvSource = environment.getPropertySources()
                .stream()
                .filter(source -> source.getName().startsWith("micro-env:"))
                .findFirst()
                .orElseThrow();

        assertEquals("8081", microEnvSource.getProperty("DEMO_PORT"));
        assertEquals("8081", microEnvSource.getProperty("demo.port"));
    }

    @Test
    void systemEnvironmentOverridesMicroEnv() throws IOException {
        writeManifest("secrets=.secret");
        writeFile("secrets/.secret", """
            DEMO_PORT=8081
            """);

        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addFirst(
                new SystemEnvironmentPropertySource(
                        "test-system-environment",
                        Map.of("DEMO_PORT", "9999")
                )
        );

        postProcessor.postProcessEnvironment(environment, null);

        assertEquals("9999", environment.getProperty("DEMO_PORT"));
        assertEquals("9999", environment.getProperty("demo.port"));
    }


    @Test
    void lowerManifestEntryOverridesHigherManifestEntry() throws IOException {
        writeManifest("""
            first=.env
            second=.env
            """);

        writeFile("first/.env", """
            API_URL=http://first
            """);

        writeFile("second/.env", """
            API_URL=http://second
            """);

        StandardEnvironment environment = new StandardEnvironment();

        postProcessor.postProcessEnvironment(environment, null);

        assertEquals("http://second", environment.getProperty("API_URL"));
        assertEquals("http://second", environment.getProperty("api.url"));
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
}
