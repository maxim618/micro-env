package dev.maxim.microenv;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
class MicroEnvPropertySourceTest {


    @Test
    void resolvesEnvironmentStyleKeysForSpringBinding() {
        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MicroEnvPropertySource(
                        "micro-env:test",
                        Map.of(
                                "DEMO_PORT", "8081",
                                "DEMO_ENABLED", "true"
                        )
                )
        );

        assertEquals("8081", environment.getProperty("demo.port"));
        assertEquals("true", environment.getProperty("demo.enabled"));
    }

    @Test
    void bindsEnvironmentStyleKeysThroughSpringBinder() {
        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MicroEnvPropertySource(
                        "micro-env:test",
                        Map.of(
                                "DEMO_PORT", "8081",
                                "DEMO_ENABLED", "true"
                        )
                )
        );

        Binder binder = Binder.get(environment);

        assertEquals(
                8081,
                binder.bind("demo.port", Bindable.of(Integer.class))
                        .orElseThrow(() -> new IllegalStateException("demo.port is not bound"))
        );

        assertTrue(
                binder.bind("demo.enabled", Bindable.of(Boolean.class))
                        .orElseThrow(() -> new IllegalStateException("demo.enabled is not bound"))
        );
    }

    @Test
    void microEnvOverridesApplicationProperties() {
        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addLast(
                new MapPropertySource(
                        "application.properties",
                        Map.of("demo.port", "8080")
                )
        );

        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MicroEnvPropertySource(
                        "micro-env:test",
                        Map.of("DEMO_PORT", "8081")
                )
        );

        Binder binder = Binder.get(environment);

        assertEquals(
                8081,
                binder.bind("demo.port", Bindable.of(Integer.class))
                        .orElseThrow(() -> new IllegalStateException("demo.port is not found"))
        );
    }


}
