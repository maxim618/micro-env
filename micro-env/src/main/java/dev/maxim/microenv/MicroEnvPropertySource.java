package dev.maxim.microenv;

import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

/**
 * Property source with the same name-resolution semantics as the OS environment,
 * so keys such as {@code DEMO_PORT} resolve as {@code demo.port} for Spring binding.
 */
final class MicroEnvPropertySource extends SystemEnvironmentPropertySource {

    MicroEnvPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }
}
