package dev.maxim.microenv;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class MicroEnvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(
            ConfigurableEnvironment environment,
            org.springframework.boot.SpringApplication application) {

        Map<String, Object> properties = Map.of(
                "micro-env.poc", "loaded"
        );

        environment.getPropertySources().addFirst(
                new MapPropertySource("micro-env-poc", properties)
        );
    }
}