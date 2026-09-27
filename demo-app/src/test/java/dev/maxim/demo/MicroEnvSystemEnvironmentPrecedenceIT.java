package dev.maxim.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class MicroEnvSystemEnvironmentPrecedenceIT {

    @Autowired
    private DemoProperties properties;

    @Test
    void systemEnvironmentOverridesMicroEnv() {
        assertEquals(9999, properties.getPort());
    }
}