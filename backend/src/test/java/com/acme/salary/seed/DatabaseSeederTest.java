package com.acme.salary.seed;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.acme.salary.employee.EmployeeRepository;

/** Runs the seeder against SQLite: inserts satisfy the schema (FKs, constraints) and re-runs are safe. */
@SpringBootTest
@ActiveProfiles("test")
class DatabaseSeederTest {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EmployeeRepository employees;

    @BeforeEach
    void cleanDatabase() {
        employees.deleteAll();
    }

    @Test
    void seedsEmptyDatabaseAndSkipsWhenDataExists() {
        DatabaseSeeder seeder = new DatabaseSeeder(jdbc, new SeedProperties(true, 500, 42, false));

        assertThat(seeder.seed()).isEqualTo(500);
        assertThat(seeder.seed()).as("second run is a no-op").isZero();
        assertThat(employees.count()).isEqualTo(500);
    }

    @Test
    void resetReplacesExistingData() {
        new DatabaseSeeder(jdbc, new SeedProperties(true, 300, 42, false)).seed();

        int inserted = new DatabaseSeeder(jdbc, new SeedProperties(true, 200, 42, true)).seed();

        assertThat(inserted).isEqualTo(200);
        assertThat(employees.count()).isEqualTo(200);
    }

    @Test
    void seededRowsAreReadableThroughJpa() {
        new DatabaseSeeder(jdbc, new SeedProperties(true, 50, 42, false)).seed();

        assertThat(employees.findAll()).allSatisfy(e -> {
            assertThat(e.getCountry().getCurrency()).isNotBlank();
            assertThat(e.getHireDate()).isNotNull();
        });
    }
}
