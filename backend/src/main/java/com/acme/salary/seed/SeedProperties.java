package com.acme.salary.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param enabled    run the seeder on startup
 * @param count      number of employees to generate
 * @param randomSeed seed for the generator; the same value always produces the same dataset
 * @param reset      delete existing employees first (otherwise seeding is skipped if any exist)
 */
@ConfigurationProperties("app.seed")
public record SeedProperties(boolean enabled, int count, long randomSeed, boolean reset) {
}
