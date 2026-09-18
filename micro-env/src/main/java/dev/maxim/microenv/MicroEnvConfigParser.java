package dev.maxim.microenv;

import java.util.ArrayList;
import java.util.List;

public class MicroEnvConfigParser {

    public List<MicroEnvConfigEntry> parse(String content) {
        List<MicroEnvConfigEntry> entries = new ArrayList<>();

        int lineNumber = 0;
        for (String rawLine : content.split("\\R")) {
            lineNumber++;

            String line = rawLine.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int separator = line.indexOf('=');
            if (separator < 0) {
                throw new IllegalArgumentException(
                        "Invalid configuration entry at line " + lineNumber + ": " + line
                );
            }

            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();

            if (key.isEmpty()) {
                throw new IllegalArgumentException(
                        "Configuration key is empty at line " + lineNumber
                );
            }

            if (value.isEmpty()) {
                throw new IllegalArgumentException(
                        "Configuration value is empty at line " + lineNumber
                );
            }

            entries.add(new MicroEnvConfigEntry(key, value));
        }

        return List.copyOf(entries);
    }
}
