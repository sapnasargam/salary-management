package com.acme.salary.seed;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.ToIntFunction;

/**
 * Generates a realistic, fully deterministic employee dataset: the same seed always yields the same rows,
 * which keeps demos, screenshots and tests reproducible.
 * <p>
 * Salaries are modelled as {@code jobBaseUsd × countryFactor × individualVariation × tenureBonus}, where
 * {@code countryFactor} combines local pay level and an approximate exchange rate so that each salary is a
 * plausible amount <em>in the country's own currency</em>. These factors exist only to produce believable
 * demo data; the application itself never converts currencies.
 */
public class EmployeeDataGenerator {

    /** Hire dates are spread over the 15 years before this fixed date (not "today", to stay deterministic). */
    public static final LocalDate REFERENCE_DATE = LocalDate.of(2025, 12, 31);
    static final int MAX_TENURE_YEARS = 15;

    record CountryProfile(String code, int weight, double payFactor) {
    }

    record JobProfile(String title, String department, long baseUsd, int weight) {
    }

    static final List<CountryProfile> COUNTRIES = List.of(
            new CountryProfile("US", 30, 1.00),
            new CountryProfile("IN", 25, 23.2),
            new CountryProfile("GB", 10, 0.57),
            new CountryProfile("DE", 8, 0.69),
            new CountryProfile("CA", 7, 1.09),
            new CountryProfile("FR", 5, 0.60),
            new CountryProfile("AU", 5, 1.23),
            new CountryProfile("SG", 4, 1.00),
            new CountryProfile("JP", 3, 82.5),
            new CountryProfile("BR", 3, 1.35));

    static final List<JobProfile> JOBS = List.of(
            new JobProfile("Software Engineer", "Engineering", 95_000, 20),
            new JobProfile("Senior Software Engineer", "Engineering", 135_000, 12),
            new JobProfile("Staff Engineer", "Engineering", 175_000, 3),
            new JobProfile("Engineering Manager", "Engineering", 165_000, 4),
            new JobProfile("QA Engineer", "Engineering", 75_000, 6),
            new JobProfile("DevOps Engineer", "Engineering", 115_000, 5),
            new JobProfile("Data Scientist", "Engineering", 125_000, 4),
            new JobProfile("Product Manager", "Product", 130_000, 4),
            new JobProfile("Product Designer", "Product", 105_000, 3),
            new JobProfile("Sales Development Representative", "Sales", 55_000, 6),
            new JobProfile("Account Executive", "Sales", 85_000, 7),
            new JobProfile("Sales Manager", "Sales", 120_000, 2),
            new JobProfile("Marketing Specialist", "Marketing", 65_000, 4),
            new JobProfile("Marketing Manager", "Marketing", 105_000, 2),
            new JobProfile("Support Specialist", "Customer Support", 45_000, 8),
            new JobProfile("Support Team Lead", "Customer Support", 62_000, 2),
            new JobProfile("Financial Analyst", "Finance", 78_000, 3),
            new JobProfile("Accountant", "Finance", 68_000, 2),
            new JobProfile("HR Generalist", "People", 62_000, 2),
            new JobProfile("Recruiter", "People", 64_000, 2));

    private static final String[] FIRST_NAMES = {
            "Aarav", "Aditi", "Akira", "Alice", "Amelia", "Ana", "Arjun", "Ben", "Camila", "Chloe", "Daniel",
            "Diya", "Emma", "Ethan", "Felix", "Gabriel", "Hana", "Hiroshi", "Isabella", "Ishaan", "Jack",
            "Julia", "Kavya", "Liam", "Lucas", "Maria", "Mateo", "Mei", "Mia", "Noah", "Olivia", "Oscar",
            "Priya", "Rahul", "Ravi", "Sakura", "Sofia", "Sophie", "Thomas", "Wei", "Yuki", "Zara"};

    private static final String[] LAST_NAMES = {
            "Anderson", "Bauer", "Brown", "Chen", "Costa", "Dubois", "Fischer", "Garcia", "Gupta", "Ito",
            "Iyer", "Johnson", "Kumar", "Lee", "Martin", "Mehta", "Muller", "Nair", "Nguyen", "Patel", "Reddy",
            "Rossi", "Santos", "Schmidt", "Sharma", "Silva", "Singh", "Smith", "Suzuki", "Tanaka", "Taylor",
            "Thompson", "Wang", "Williams", "Wilson", "Yamamoto"};

    private static final double VARIATION_STD_DEV = 0.12;
    private static final double TENURE_BONUS_PER_YEAR = 0.015;

    public List<SeedEmployee> generate(int count, long randomSeed) {
        Random random = new Random(randomSeed);
        List<SeedEmployee> employees = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            employees.add(generateOne(i, random));
        }
        return employees;
    }

    private SeedEmployee generateOne(int sequence, Random random) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        CountryProfile country = pickWeighted(COUNTRIES, CountryProfile::weight, random);
        JobProfile job = pickWeighted(JOBS, JobProfile::weight, random);

        int tenureDays = random.nextInt(MAX_TENURE_YEARS * 365);
        LocalDate hireDate = REFERENCE_DATE.minusDays(tenureDays);

        double variation = clamp(1 + random.nextGaussian() * VARIATION_STD_DEV, 0.75, 1.35);
        double tenureBonus = 1 + TENURE_BONUS_PER_YEAR * (tenureDays / 365);
        long salary = roundToHundreds(job.baseUsd() * country.payFactor() * variation * tenureBonus);

        // The sequence number guarantees uniqueness; names alone repeat across 10k employees.
        String email = (firstName + "." + lastName + sequence).toLowerCase(Locale.ROOT) + "@acme.com";
        return new SeedEmployee(firstName + " " + lastName, email, job.title(), job.department(), country.code(),
                salary, hireDate);
    }

    static <T> T pickWeighted(List<T> options, ToIntFunction<T> weight, Random random) {
        int total = options.stream().mapToInt(weight).sum();
        int roll = random.nextInt(total);
        for (T option : options) {
            roll -= weight.applyAsInt(option);
            if (roll < 0) {
                return option;
            }
        }
        throw new IllegalStateException("unreachable: weights must be positive");
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static long roundToHundreds(double amount) {
        return Math.max(100, Math.round(amount / 100) * 100);
    }
}
