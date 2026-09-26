package dev.maxim.microenv;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MicroEnvPropertySourcePrecedenceTest {

    @Test
    void lowerManifestEntryOverridesHigherManifestEntry() {
        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MicroEnvPropertySource(
                        "micro-env:first",
                        Map.of("API_URL", "http://first")
                )
        );

        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MicroEnvPropertySource(
                        "micro-env:second",
                        Map.of("API_URL", "http://second")
                )
        );

        assertEquals("http://second", environment.getProperty("API_URL"));
    }
}