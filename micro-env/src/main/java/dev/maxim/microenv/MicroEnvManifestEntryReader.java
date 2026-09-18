package dev.maxim.microenv;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MicroEnvManifestEntryReader {

    public List<Path> resolveEntries(MicroEnvManifest manifest) {
        Path manifestDirectory = manifest.path().getParent();
        List<Path> paths = new ArrayList<>();

        for (String rawLine : manifest.content().split("\\R")) {
            String line = rawLine.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int separator = line.indexOf('=');
            if (separator < 0) {
                throw new IllegalArgumentException("Invalid manifest entry: " + line);
            }

            String directory = line.substring(0, separator).trim();
            String fileName = line.substring(separator + 1).trim();

            if (directory.isEmpty() || fileName.isEmpty()) {
                throw new IllegalArgumentException("Invalid manifest entry: " + line);
            }

            Path directoryPath = Path.of(directory);
            Path fileNamePath = Path.of(fileName);

            if (directoryPath.isAbsolute()
                    || fileNamePath.getNameCount() != 1
                    || fileName.contains("/") 
                    || fileName.contains("\\")) {
                throw new IllegalArgumentException("Invalid manifest entry: " + line);
            }

            Path configPath = manifestDirectory
                    .resolve(directoryPath)
                    .resolve(fileNamePath)
                    .normalize()
                    .toAbsolutePath();

            paths.add(configPath);
        }

        return List.copyOf(paths);
    }
}
