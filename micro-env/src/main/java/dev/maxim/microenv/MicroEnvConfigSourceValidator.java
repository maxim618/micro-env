package dev.maxim.microenv;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MicroEnvConfigSourceValidator {

    public void validate(List<Path> paths) {
        Set<Path> seen = new HashSet<>();

        for (Path path : paths) {
            if (!seen.add(path)) {
                throw new IllegalArgumentException(
                        "Duplicate configuration source: " + path
                );
            }
        }
    }
}
