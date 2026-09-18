package dev.maxim.microenv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MicroEnvConfigFileReader {

    public MicroEnvConfigFile read(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        return new MicroEnvConfigFile(path, content);
    }
}
