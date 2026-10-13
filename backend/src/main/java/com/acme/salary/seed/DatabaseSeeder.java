package com.acme.salary.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the database with generated employees on startup.
 * <p>
 * Idempotent: if employees already exist nothing happens (unless {@code app.seed.reset=true}), so it is
 * safe to leave enabled. Uses JDBC batch inserts in one transaction — 10k rows take well under a second,
 * whereas JPA with IDENTITY ids cannot batch inserts at all.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DatabaseSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final int BATCH_SIZE = 1_000;
    private static final String INSERT_SQL = """
            INSERT INTO employee (full_name, email, job_title, department, country_code, annual_salary, hire_date)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbc;
    private final SeedProperties properties;
    private final EmployeeDataGenerator generator;

    public DatabaseSeeder(JdbcTemplate jdbc, SeedProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
        this.generator = new EmployeeDataGenerator();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seed();
    }

    /** @return the number of employees inserted (0 when skipped) */
    @Transactional
    public int seed() {
        long existing = jdbc.queryForObject("SELECT COUNT(*) FROM employee", Long.class);
        if (existing > 0 && !properties.reset()) {
            log.info("Seeding skipped: database already has {} employees", existing);
            return 0;
        }
        if (properties.reset()) {
            jdbc.update("DELETE FROM employee");
        }

        long start = System.nanoTime();
        List<SeedEmployee> employees = generator.generate(properties.count(), properties.randomSeed());
        jdbc.batchUpdate(INSERT_SQL, employees, BATCH_SIZE, (statement, employee) -> {
            statement.setString(1, employee.fullName());
            statement.setString(2, employee.email());
            statement.setString(3, employee.jobTitle());
            statement.setString(4, employee.department());
            statement.setString(5, employee.countryCode());
            statement.setLong(6, employee.annualSalary());
            statement.setString(7, employee.hireDate().toString());
        });
        log.info("Seeded {} employees in {} ms", employees.size(), (System.nanoTime() - start) / 1_000_000);
        return employees.size();
    }
}
