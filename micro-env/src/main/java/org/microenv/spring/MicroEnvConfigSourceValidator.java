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

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the configuration file sources before they are loaded.
 */
public class MicroEnvConfigSourceValidator {

    /**
     * Verifies that each configuration source occurs only once.
     *
     * @param paths configuration file paths to validate
     * @throws IllegalArgumentException if the same path occurs more than once
     */
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
