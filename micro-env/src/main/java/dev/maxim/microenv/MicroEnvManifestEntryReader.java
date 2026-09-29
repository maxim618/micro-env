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
