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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MicroEnvConfigLoaderTest {

    @TempDir
    Path tempDir;

    private final MicroEnvConfigLoader loader = new MicroEnvConfigLoader();

    @Test
    void loadsEachConfigurationFileAsSeparateResult() throws IOException {
        Path common = writeFile(
                "common.env",
                """
                COMMON=common-value
                FIRST=first-value
                """
        );
        Path local = writeFile(
                "local.env",
                """
                SECOND=second-value
                """
        );

        List<MicroEnvConfig> result = loader.load(List.of(common, local));

        assertEquals(2, result.size());

        assertEquals(common, result.get(0).path());
        assertEquals(
                List.of(
                        new MicroEnvConfigEntry("COMMON", "common-value"),
                        new MicroEnvConfigEntry("FIRST", "first-value")
                ),
                result.get(0).entries()
        );

        assertEquals(local, result.get(1).path());
        assertEquals(
                List.of(
                        new MicroEnvConfigEntry("SECOND", "second-value")
                ),
                result.get(1).entries()
        );
    }

    @Test
    void preservesInputFileOrder() throws IOException {
        Path first = writeFile("first.env", "VALUE=from-first");
        Path second = writeFile("second.env", "VALUE=from-second");

        List<MicroEnvConfig> result = loader.load(List.of(first, second));

        assertEquals(
                List.of(first, second),
                result.stream()
                        .map(MicroEnvConfig::path)
                        .toList()
        );
    }

    @Test
    void preservesSameKeyFromDifferentFiles() throws IOException {
        Path common = writeFile("common.env", "SHARED=from-common");
        Path local = writeFile("local.env", "SHARED=from-local");

        List<MicroEnvConfig> result = loader.load(List.of(common, local));

        assertEquals(
                List.of(
                        new MicroEnvConfigEntry("SHARED", "from-common")
                ),
                result.get(0).entries()
        );

        assertEquals(
                List.of(
                        new MicroEnvConfigEntry("SHARED", "from-local")
                ),
                result.get(1).entries()
        );
    }

    @Test
    void invalidLaterFilePreventsReturningAnyConfiguration() throws IOException {
        Path common = writeFile("common.env", "COMMON=common-value");
        Path broken = writeFile("broken.env", "BROKEN");

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(List.of(common, broken))
        );
    }

    @Test
    void missingLaterFilePreventsReturningAnyConfiguration() throws IOException {
        Path common = writeFile("common.env", "COMMON=common-value");
        Path missing = tempDir.resolve("missing.env");

        assertThrows(
                IOException.class,
                () -> loader.load(List.of(common, missing))
        );
    }

    @Test
    void duplicateSourceIsRejectedBeforeReadingFiles() {
        Path source = tempDir.resolve("common.env");

        assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(List.of(source, source))
        );
    }

    private Path writeFile(String fileName, String content) throws IOException {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, content);
        return path;
    }
}
