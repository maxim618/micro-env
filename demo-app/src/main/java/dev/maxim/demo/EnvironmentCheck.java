package dev.maxim.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentCheck implements CommandLineRunner {

    private final Environment environment;

    public EnvironmentCheck(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        String value = environment.getProperty("micro-env.poc");

        System.out.println("micro-env.poc = " + value);
    }
}