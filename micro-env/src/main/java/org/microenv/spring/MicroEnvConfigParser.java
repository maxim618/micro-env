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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MicroEnvConfigParser {

    public List<MicroEnvConfigEntry> parse(String content) {
        List<MicroEnvConfigEntry> entries = new ArrayList<>();
        Set<String> keys = new HashSet<>();

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

            if (!keys.add(key)) {
                throw new IllegalArgumentException(
                        "Duplicate configuration key at line " + lineNumber + ": " + key
                );
            }

            entries.add(new MicroEnvConfigEntry(key, value));
        }

        return List.copyOf(entries);
    }
}
