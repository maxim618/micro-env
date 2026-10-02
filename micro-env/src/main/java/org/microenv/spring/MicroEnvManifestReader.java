/*
 * Copyright 2026 Maxim Butmanov
 * SPDX-License-Identifier: Apache-2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.microenv.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.system.ApplicationHome;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Finds and reads the micro-env manifest used by the application.
 */
class MicroEnvManifestReader {

    private static final System.Logger LOGGER =
            System.getLogger(MicroEnvManifestReader.class.getName());

    /**
     * Default manifest file name.
     */
    static final String DEFAULT_MANIFEST_NAME = "micro-env.list";

    /**
     * System property used to specify an explicit manifest path.
     */
    static final String MANIFEST_PROPERTY = "micro.env.manifest";

    /**
     * Reads the configured manifest or searches for the default manifest.
     *
     * <p>If {@value #MANIFEST_PROPERTY} is set, its value is used as the manifest
     * path. Otherwise, {@value #DEFAULT_MANIFEST_NAME} is searched for from the
     * application's directory through its parent directories.</p>
     *
     * @param application Spring application used to determine the application location
     * @return the manifest when found and readable, otherwise an empty optional
     */
    Optional<MicroEnvManifest> readDefaultManifest(
            SpringApplication application) {

        String configuredManifest = System.getProperty(MANIFEST_PROPERTY);
        if (configuredManifest != null && !configuredManifest.isBlank()) {
            try {
                return readManifestAt(Path.of(configuredManifest));
            } catch (IllegalArgumentException e) {
                LOGGER.log(
                        System.Logger.Level.WARNING,
                        "Failed to read micro-env manifest: invalid path",
                        e
                );
                return Optional.empty();
            }
        }

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
                return readManifestAt(manifestPath);
            }

            current = current.getParent();
        }

        return Optional.empty();
    }

    private Optional<MicroEnvManifest> readManifestAt(Path manifestPath) {
        Path normalizedPath = manifestPath.toAbsolutePath().normalize();

        if (!Files.isRegularFile(normalizedPath)) {
            LOGGER.log(
                    System.Logger.Level.WARNING,
                    "Failed to read micro-env manifest: path is not a regular file: "
                            + normalizedPath
            );
            return Optional.empty();
        }

        try {
            String content = Files.readString(
                    normalizedPath,
                    StandardCharsets.UTF_8
            );

            return Optional.of(new MicroEnvManifest(
                    normalizedPath,
                    content
            ));
        } catch (IOException e) {
            LOGGER.log(
                    System.Logger.Level.WARNING,
                    "Failed to read micro-env manifest: " + normalizedPath,
                    e
            );
            return Optional.empty();
        }
    }
}
