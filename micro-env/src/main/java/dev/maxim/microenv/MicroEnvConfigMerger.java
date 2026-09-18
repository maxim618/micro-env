package dev.maxim.microenv;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MicroEnvConfigMerger {

    private final MicroEnvConfigParser parser = new MicroEnvConfigParser();

    public Map<String, String> merge(List<MicroEnvConfigFile> files) {
        Map<String, String> properties = new LinkedHashMap<>();

        for (MicroEnvConfigFile file : files) {
            for (MicroEnvConfigEntry entry : parser.parse(file.content())) {
                properties.put(entry.key(), entry.value());
            }
        }

        return Map.copyOf(properties);
    }
}
