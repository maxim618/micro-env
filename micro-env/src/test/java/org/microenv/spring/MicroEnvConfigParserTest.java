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

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MicroEnvConfigParserTest {

    private final MicroEnvConfigParser parser = new MicroEnvConfigParser();

    @Test
    void parsesDistinctKeys() {
        List<MicroEnvConfigEntry> entries = parser.parse("""
                FIRST=one
                SECOND=two
                """);

        assertEquals(
                List.of(
                        new MicroEnvConfigEntry("FIRST", "one"),
                        new MicroEnvConfigEntry("SECOND", "two")
                ),
                entries
        );
    }

    @Test
    void rejectsDuplicateKeyWithinOneFile() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("""
                        FIRST=one
                        FIRST=two
                        """)
        );

        assertEquals("Duplicate configuration key at line 2: FIRST", exception.getMessage());
    }

    @Test
    void treatsWhitespaceAroundKeyAsSameKey() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parser.parse("""
                        FIRST=one
                         FIRST = two
                        """)
        );

        assertEquals("Duplicate configuration key at line 2: FIRST", exception.getMessage());
    }

    @Test
    void allowsEqualsInsideValue() {
        List<MicroEnvConfigEntry> entries = parser.parse("URL=https://example.test?a=b");

        assertEquals(
                List.of(new MicroEnvConfigEntry("URL", "https://example.test?a=b")),
                entries
        );
    }

    @Test
    void ignoresEmptyLinesAndComments() {
        List<MicroEnvConfigEntry> entries = parser.parse("""
                
                # comment
                VALUE=test
                
                """);

        assertEquals(
                List.of(new MicroEnvConfigEntry("VALUE", "test")),
                entries
        );
    }
}
