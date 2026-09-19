package dev.maxim.microenv;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MicroEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final System.Logger LOGGER =
            System.getLogger(MicroEnvEnvironmentPostProcessor.class.getName());

    private static final String PROPERTY_SOURCE_NAME = "micro-env";

    private final MicroEnvManifestReader manifestReader = new MicroEnvManifestReader();
    private final MicroEnvManifestEntryReader entryReader = new MicroEnvManifestEntryReader();
    private final MicroEnvConfigLoader configLoader = new MicroEnvConfigLoader();

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            org.springframework.boot.SpringApplication application) {

        Optional<MicroEnvManifest> manifest = manifestReader.readDefaultManifest();

        if (manifest.isEmpty()) {
            return;
        }

        try {
            List<Path> paths = entryReader.resolveEntries(manifest.get());
            Map<String, String> properties = configLoader.load(paths);

            environment.getPropertySources().addFirst(
                    new MapPropertySource(PROPERTY_SOURCE_NAME, properties)
            );
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(
                    System.Logger.Level.WARNING,
                    "Failed to load micro-env configuration",
                    e
            );
        }
    }
}
