package dev.maxim.microenv;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MicroEnvConfigMergerTest {

    private final MicroEnvConfigMerger merger = new MicroEnvConfigMerger();

    @Test
    void mergesValuesFromMultipleFiles() {
        List<MicroEnvConfigFile> files = List.of(
                new MicroEnvConfigFile(
                        Path.of("common.env"),
                        """
                        COMMON=common-value
                        FIRST=first-value
                        """
                ),
                new MicroEnvConfigFile(
                        Path.of("local.env"),
                        """
                        SECOND=second-value
                        """
                )
        );

        assertEquals(
                Map.of(
                        "COMMON", "common-value",
                        "FIRST", "first-value",
                        "SECOND", "second-value"
                ),
                merger.merge(files)
        );
    }

    @Test
    void laterFileOverridesEarlierFile() {
        List<MicroEnvConfigFile> files = List.of(
                new MicroEnvConfigFile(
                        Path.of("common.env"),
                        "SHARED=from-common"
                ),
                new MicroEnvConfigFile(
                        Path.of("local.env"),
                        "SHARED=from-local"
                )
        );

        assertEquals(
                Map.of("SHARED", "from-local"),
                merger.merge(files)
        );
    }

    @Test
    void earlierFileRemainsWhenLaterFileDoesNotDefineSameKey() {
        List<MicroEnvConfigFile> files = List.of(
                new MicroEnvConfigFile(
                        Path.of("common.env"),
                        "SHARED=from-common"
                ),
                new MicroEnvConfigFile(
                        Path.of("local.env"),
                        "LOCAL=local-value"
                )
        );

        assertEquals(
                Map.of(
                        "SHARED", "from-common",
                        "LOCAL", "local-value"
                ),
                merger.merge(files)
        );
    }
}
