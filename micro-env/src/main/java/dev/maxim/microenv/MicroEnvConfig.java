package dev.maxim.microenv;

import java.nio.file.Path;
import java.util.List;

public record MicroEnvConfig(Path path, List<MicroEnvConfigEntry> entries) {

    public MicroEnvConfig {
        entries = List.copyOf(entries);
    }
}
