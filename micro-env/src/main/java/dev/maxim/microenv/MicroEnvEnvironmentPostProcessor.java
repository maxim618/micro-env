package dev.maxim.microenv;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MicroEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final System.Logger LOGGER =
            System.getLogger(MicroEnvEnvironmentPostProcessor.class.getName());

    private static final String PROPERTY_SOURCE_PREFIX = "micro-env:";

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
            List<MicroEnvConfig> configs = configLoader.load(paths);
            List<PropertySource<?>> propertySources = createPropertySources(configs);

            addPropertySources(environment, propertySources);
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.log(
                    System.Logger.Level.WARNING,
                    "Failed to load micro-env configuration: configuration loading was not completed; no properties were applied",
                    e
            );
        }
    }

    private List<PropertySource<?>> createPropertySources(
            List<MicroEnvConfig> configs) {

        List<PropertySource<?>> propertySources = new ArrayList<>(configs.size());

        for (MicroEnvConfig config : configs) {
            Map<String, Object> properties = new LinkedHashMap<>();

            for (MicroEnvConfigEntry entry : config.entries()) {
                properties.put(entry.key(), entry.value());
            }

            propertySources.add(
                    new MapPropertySource(
                            PROPERTY_SOURCE_PREFIX + config.path(),
                            properties
                    )
            );
        }

        return List.copyOf(propertySources);
    }

    private void addPropertySources(
            ConfigurableEnvironment environment,
            List<PropertySource<?>> propertySources) {

        MutablePropertySources sources = environment.getPropertySources();
        String systemEnvironment = StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

        for (PropertySource<?> propertySource : propertySources) {
            sources.addAfter(systemEnvironment, propertySource);
        }
    }
}
