package dev.maxim.microenv;

import java.nio.file.Path;

public record MicroEnvConfigFile(Path path, String content) {
}
