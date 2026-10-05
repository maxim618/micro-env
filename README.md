# micro-env

![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)
[![CI](https://github.com/maxim618/micro-env/actions/workflows/ci.yml/badge.svg)](https://github.com/maxim618/micro-env/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

`micro-env` is a small Spring Boot library for loading local configuration values from explicitly configured files into the Spring `Environment`.

It is intended for local development when secrets such as API keys or passwords should stay outside Git without requiring OS environment variables or application-specific initialization code.

## Installation

Add `micro-env` as a Maven dependency:

```xml
<dependency>
    <groupId>io.github.maxim618</groupId>
    <artifactId>micro-env</artifactId>
    <version>0.1.0</version>
</dependency>
```

Create a `micro-env.list` file in the application working directory and configure the local configuration sources. For example:

```text
secrets/openrouter=.env
secrets/database=.env
```

Then create the referenced files with `KEY=VALUE` entries. No explicit initialization code is required in the application; `micro-env` integrates with Spring Boot automatically.

`micro-env` is required at runtime and remains on the application's classpath. Configuration files containing secrets stay outside the application JAR.

## Features

* Multiple local configuration files
* A simple `KEY=VALUE` format
* `micro-env.list` manifest with explicit source ordering
* Deterministic override rules
* Atomic configuration loading: validate first, apply second
* Automatic Spring Boot integration through `EnvironmentPostProcessor`
* No changes to the OS environment
* No persistence or caching of loaded values
* No secret values in diagnostic messages
* Minimal dependencies and a deliberately small scope

## How it works

A manifest defines the configuration sources:

```text
secrets/openrouter=.env
secrets/database=.env
```

The paths are resolved relative to the directory containing `micro-env.list`.

Each configuration file uses a minimal format:

```text
OPENROUTER_API_KEY=local-key
DATABASE_PASSWORD=local-password
```

The files are loaded into the Spring `Environment` as separate `PropertySource` instances.

If the same key occurs in different files, the value from the file **lower in `micro-env.list` wins**:

```text
secrets/defaults=.env
secrets/local=.env
```

This makes the manifest both a list of configuration sources and an explicit definition of their priority.

Duplicate keys **within the same file are not allowed** and cause the whole `micro-env` configuration to be rejected.

## Example

A project can have the following structure:

```text
project/
├── micro-env.list
├── secrets/
│   ├── openrouter/
│   │   └── .env
│   └── database/
│       └── .env
└── application/
    └── ...
```

`micro-env.list`:

```text
secrets/openrouter=.env
secrets/database=.env
```

`secrets/openrouter/.env`:

```text
OPENROUTER_API_KEY=local-key
```

`secrets/database/.env`:

```text
DATABASE_PASSWORD=local-password
```

After adding the library as a dependency, no explicit initialization code is required in the application.

## Configuration format

The supported format is intentionally small:

```text
KEY=VALUE
```

Supported:

* comments starting with `#`
* empty lines
* whitespace around keys and values
* `=` inside values

The library does **not** attempt to implement the complete dotenv specification.

For example, constructs such as the following are outside the supported format:

```text
export KEY=value
KEY="value"
KEY='value'
KEY=${OTHER}
```

## Configuration priority

`micro-env` uses Spring's existing property resolution model rather than introducing its own global precedence system.

Its position is below OS environment variables and above application Config Data:

```text
command-line arguments
SPRING_APPLICATION_JSON
system properties
OS environment variables
micro-env
application.properties / application.yml / other Config Data
```

This means an explicitly supplied external configuration value can override a value loaded from `micro-env`.

## Error handling

Configuration is validated completely before anything is applied.

If the manifest or any referenced configuration file is invalid, `micro-env`:

1. reports a diagnostic warning;
2. applies nothing from `micro-env`;
3. leaves the existing Spring configuration unchanged.

Secret values are never included in diagnostic output.

## Scope

`micro-env` is designed for **local development configuration**.

It is not intended to replace production secret-management systems such as Vault, cloud secret managers, Kubernetes Secrets, or platform-provided secret mechanisms.

The goal is deliberately narrower: provide a small and predictable way to keep local configuration outside source control while integrating naturally with Spring Boot.

## Technologies

* Java
* Spring Boot
* Spring `Environment` ; `EnvironmentPostProcessor`
* Maven
* JUnit

## License

This project is licensed under the [Apache License 2.0](LICENSE).
