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
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
class MicroEnvPropertySourceTest {


    @Test
    void resolvesEnvironmentStyleKeysForSpringBinding() {
        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MicroEnvPropertySource(
                        "micro-env:test",
                        Map.of(
                                "DEMO_PORT", "8081",
                                "DEMO_ENABLED", "true"
                        )
                )
        );

        assertEquals("8081", environment.getProperty("demo.port"));
        assertEquals("true", environment.getProperty("demo.enabled"));
    }

    @Test
    void bindsEnvironmentStyleKeysThroughSpringBinder() {
        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MicroEnvPropertySource(
                        "micro-env:test",
                        Map.of(
                                "DEMO_PORT", "8081",
                                "DEMO_ENABLED", "true"
                        )
                )
        );

        Binder binder = Binder.get(environment);

        assertEquals(
                8081,
                binder.bind("demo.port", Bindable.of(Integer.class))
                        .orElseThrow(() -> new IllegalStateException("demo.port is not bound"))
        );

        assertTrue(
                binder.bind("demo.enabled", Bindable.of(Boolean.class))
                        .orElseThrow(() -> new IllegalStateException("demo.enabled is not bound"))
        );
    }

    @Test
    void microEnvOverridesApplicationProperties() {
        StandardEnvironment environment = new StandardEnvironment();

        environment.getPropertySources().addLast(
                new MapPropertySource(
                        "application.properties",
                        Map.of("demo.port", "8080")
                )
        );

        environment.getPropertySources().addAfter(
                StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new MicroEnvPropertySource(
                        "micro-env:test",
                        Map.of("DEMO_PORT", "8081")
                )
        );

        Binder binder = Binder.get(environment);

        assertEquals(
                8081,
                binder.bind("demo.port", Bindable.of(Integer.class))
                        .orElseThrow(() -> new IllegalStateException("demo.port is not found"))
        );
    }


}
