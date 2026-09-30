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

import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

/**
 * Property source with the same name-resolution semantics as the OS environment,
 * so keys such as {@code DEMO_PORT} resolve as {@code demo.port} for Spring binding.
 */
final class MicroEnvPropertySource extends SystemEnvironmentPropertySource {

    MicroEnvPropertySource(String name, Map<String, Object> source) {
        super(name, source);
    }
}
