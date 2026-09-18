package dev.maxim.microenv;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MicroEnvConfigMerger {

    private final MicroEnvConfigParser parser = new MicroEnvConfigParser();

    public Map<String, String> merge(List<MicroEnvConfigFile> files) {
        List<MicroEnvConfigEntry> entries = new ArrayList<>();

        for (MicroEnvConfigFile file : files) {
            entries.addAll(parser.parse(file.content()));
        }

        Map<String, String> properties = new LinkedHashMap<>();
        for (MicroEnvConfigEntry entry : entries) {
            properties.put(entry.key(), entry.value());
        }

        return Map.copyOf(properties);
    }
}
