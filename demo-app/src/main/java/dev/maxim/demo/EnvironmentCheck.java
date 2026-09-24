package dev.maxim.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentCheck implements CommandLineRunner {

    private final String demoValue;
    private final DemoProperties properties;

    public EnvironmentCheck(
            @Value("${DEMO_VALUE}") String demoValue,
            DemoProperties properties) {
        this.demoValue = demoValue;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== PROPERTY DIAGNOSTICS ===");

        System.out.println("DEMO_VALUE via @Value = " + demoValue);

        System.out.println("@ConfigurationProperties demo.port = "
                + properties.getPort());

        System.out.println("@ConfigurationProperties demo.enabled = "
                + properties.isEnabled());

        System.out.println("=== END PROPERTY DIAGNOSTICS ===");

    }
}