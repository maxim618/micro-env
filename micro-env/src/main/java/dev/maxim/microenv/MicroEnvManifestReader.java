package dev.maxim.microenv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.system.ApplicationHome;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class MicroEnvManifestReader {

    private static final System.Logger LOGGER =
            System.getLogger(MicroEnvManifestReader.class.getName());

    public static final String DEFAULT_MANIFEST_NAME = "micro-env.list";

    public Optional<MicroEnvManifest> readDefaultManifest(
            SpringApplication application) {

        Class<?> mainApplicationClass = application.getMainApplicationClass();
        if (mainApplicationClass == null) {
            return Optional.empty();
        }

        ApplicationHome applicationHome =
                new ApplicationHome(mainApplicationClass);

        return readManifestFrom(applicationHome.getDir().toPath());
    }

    Optional<MicroEnvManifest> readManifestFrom(Path applicationLocation) {
        Path current = applicationLocation.toAbsolutePath().normalize();

        while (current != null) {
            Path manifestPath = current.resolve(DEFAULT_MANIFEST_NAME);

            if (Files.isRegularFile(manifestPath)) {
                try {
                    String content = Files.readString(
                            manifestPath,
                            StandardCharsets.UTF_8
                    );

                    return Optional.of(new MicroEnvManifest(
                            manifestPath,
                            content
                    ));
                } catch (IOException e) {
                    LOGGER.log(
                            System.Logger.Level.WARNING,
                            "Failed to read micro-env manifest: " + manifestPath,
                            e
                    );
                    return Optional.empty();
                }
            }

            current = current.getParent();
        }

        return Optional.empty();
    }
}
