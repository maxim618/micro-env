package dev.maxim.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MicroEnvConfigurationPropertiesTest {

    @Autowired
    private DemoProperties properties;

    @Test
    void bindsMicroEnvPropertiesToConfigurationProperties() {
        assertEquals(8081, properties.getPort());
        assertTrue(properties.isEnabled());
    }
}