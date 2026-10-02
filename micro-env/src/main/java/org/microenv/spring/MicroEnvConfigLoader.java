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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates, reads, and parses micro-env configuration sources.
 */
class MicroEnvConfigLoader {

    private final MicroEnvConfigSourceValidator sourceValidator;
    private final MicroEnvConfigFileReader fileReader;
    private final MicroEnvConfigParser parser;

    /**
     * Creates a loader with the default validation, file-reading, and parsing components.
     */
    MicroEnvConfigLoader() {
        this(
                new MicroEnvConfigSourceValidator(),
                new MicroEnvConfigFileReader(),
                new MicroEnvConfigParser()
        );
    }

    MicroEnvConfigLoader(
            MicroEnvConfigSourceValidator sourceValidator,
            MicroEnvConfigFileReader fileReader,
            MicroEnvConfigParser parser
    ) {
        this.sourceValidator = sourceValidator;
        this.fileReader = fileReader;
        this.parser = parser;
    }

    /**
     * Loads and parses the supplied configuration files.
     *
     * @param paths configuration file paths in manifest order
     * @return parsed configurations in the same order as the supplied paths
     * @throws IOException if a configuration file cannot be read
     * @throws IllegalArgumentException if duplicate configuration sources are supplied
     *         or a configuration entry is invalid
     */
    List<MicroEnvConfig> load(List<Path> paths) throws IOException {
        sourceValidator.validate(paths);

        List<MicroEnvConfigFile> files = new ArrayList<>(paths.size());
        for (Path path : paths) {
            files.add(fileReader.read(path));
        }

        List<MicroEnvConfig> configs = new ArrayList<>(files.size());
        for (MicroEnvConfigFile file : files) {
            configs.add(new MicroEnvConfig(
                    file.path(),
                    parser.parse(file.content())
            ));
        }

        return List.copyOf(configs);
    }
}
