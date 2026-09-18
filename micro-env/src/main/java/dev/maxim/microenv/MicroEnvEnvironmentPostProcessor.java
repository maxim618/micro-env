package dev.maxim.microenv;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class MicroEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private final MicroEnvManifestReader manifestReader = new MicroEnvManifestReader();
    private final MicroEnvManifestEntryReader entryReader = new MicroEnvManifestEntryReader();

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            org.springframework.boot.SpringApplication application) {

        manifestReader.readDefaultManifest()
                .ifPresent(entryReader::resolveEntries);

        Map<String, Object> properties = Map.of(
                "micro-env.poc", "loaded"
        );

        environment.getPropertySources().addFirst(
                new MapPropertySource("micro-env-poc", properties)
        );
    }
}
