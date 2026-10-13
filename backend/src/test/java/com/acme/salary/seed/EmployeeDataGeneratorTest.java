package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmployeeDataGeneratorTest {

    private static final EmployeeDataGenerator GENERATOR = new EmployeeDataGenerator();
    private static List<SeedEmployee> employees;

    @BeforeAll
    static void generateOnce() {
        employees = GENERATOR.generate(10_000, 42);
    }

    @Test
    void generatesRequestedNumberOfEmployees() {
        assertThat(employees).hasSize(10_000);
    }

    @Test
    void sameSeedProducesIdenticalDatasetAndDifferentSeedDoesNot() {
        assertThat(GENERATOR.generate(100, 42)).isEqualTo(employees.subList(0, 100));
        assertThat(GENERATOR.generate(100, 7)).isNotEqualTo(employees.subList(0, 100));
    }

    @Test
    void emailsAreUnique() {
        assertThat(employees.stream().map(SeedEmployee::email).distinct().count()).isEqualTo(10_000);
    }

    @Test
    void everyRowIsValid() {
        assertThat(employees).allSatisfy(e -> {
            assertThat(e.fullName()).isNotBlank();
            assertThat(e.email()).matches("[a-z]+\\.[a-z]+\\d+@acme\\.com");
            assertThat(e.annualSalary()).isPositive();
            assertThat(e.annualSalary() % 100).isZero();
            assertThat(e.hireDate()).isBeforeOrEqualTo(EmployeeDataGenerator.REFERENCE_DATE)
                    .isAfter(EmployeeDataGenerator.REFERENCE_DATE.minusYears(EmployeeDataGenerator.MAX_TENURE_YEARS));
        });
    }

    @Test
    void usesOnlyKnownCountriesAndCoversAllOfThem() {
        Set<String> known = EmployeeDataGenerator.COUNTRIES.stream()
                .map(EmployeeDataGenerator.CountryProfile::code).collect(Collectors.toSet());
        assertThat(employees.stream().map(SeedEmployee::countryCode).collect(Collectors.toSet())).isEqualTo(known);
    }

    @Test
    void countryDistributionFollowsWeights() {
        Map<String, Long> perCountry = employees.stream()
                .collect(Collectors.groupingBy(SeedEmployee::countryCode, Collectors.counting()));
        // US weight is 30% and JP 3%; allow sampling noise.
        assertThat(perCountry.get("US")).isBetween(2_800L, 3_200L);
        assertThat(perCountry.get("JP")).isBetween(200L, 400L);
    }

    @Test
    void salariesAreInLocalCurrencyScale() {
        double avgUs = averageSalary("US", "Software Engineer");
        double avgIndia = averageSalary("IN", "Software Engineer");
        double avgJapan = averageSalary("JP", "Software Engineer");

        assertThat(avgUs).isBetween(90_000.0, 130_000.0);       // USD
        assertThat(avgIndia).isBetween(2_000_000.0, 3_000_000.0); // INR
        assertThat(avgJapan).isBetween(7_000_000.0, 11_000_000.0); // JPY
    }

    @Test
    void seniorRolesEarnMoreThanJuniorRolesInSameCountry() {
        assertThat(averageSalary("US", "Senior Software Engineer"))
                .isGreaterThan(averageSalary("US", "Software Engineer"));
    }

    @Test
    void pickWeightedRespectsWeights() {
        Random random = new Random(1);
        List<String> options = List.of("never", "always");
        Map<String, Integer> weights = Map.of("never", 0, "always", 1);

        for (int i = 0; i < 100; i++) {
            assertThat(EmployeeDataGenerator.pickWeighted(options, weights::get, random)).isEqualTo("always");
        }
    }

    private static double averageSalary(String country, String jobTitle) {
        return employees.stream()
                .filter(e -> e.countryCode().equals(country) && e.jobTitle().equals(jobTitle))
                .mapToLong(SeedEmployee::annualSalary)
                .average().orElseThrow();
    }
}
