package dev.maxim.microenv;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class MicroEnvManifestReader {

    private static final System.Logger LOGGER =
            System.getLogger(MicroEnvManifestReader.class.getName());

    public static final String DEFAULT_MANIFEST_NAME = "micro-env.list";

    public Optional<MicroEnvManifest> readDefaultManifest() {
        Path manifestPath = Path.of(System.getProperty("user.dir"))
                .resolve(DEFAULT_MANIFEST_NAME);

        if (!Files.exists(manifestPath)) {
            return Optional.empty();
        }

        try {
            String content = Files.readString(manifestPath, StandardCharsets.UTF_8);
            return Optional.of(new MicroEnvManifest(manifestPath.toAbsolutePath().normalize(), content));
        } catch (IOException e) {
            LOGGER.log(
                    System.Logger.Level.WARNING,
                    "Failed to read micro-env manifest: " + manifestPath,
                    e
            );
            return Optional.empty();
        }
    }
}
