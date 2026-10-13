package com.acme.salary.insights;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.acme.salary.country.CountryRepository;
import com.acme.salary.employee.Employee;
import com.acme.salary.employee.EmployeeRepository;

/** Verifies the JPQL projections and the JSON shape of insights and reference data against SQLite. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InsightsApiIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private EmployeeRepository employees;
    @Autowired
    private CountryRepository countries;

    @BeforeEach
    void seed() {
        employees.deleteAll();
        save("a@acme.com", "Software Engineer", "Engineering", "US", 100_000);
        save("b@acme.com", "Software Engineer", "Engineering", "US", 140_000);
        save("c@acme.com", "Account Executive", "Sales", "US", 90_000);
        save("d@acme.com", "Software Engineer", "Engineering", "IN", 2_000_000);
    }

    @Test
    void overviewListsPerCountryStatistics() throws Exception {
        mvc.perform(get("/api/insights/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalHeadcount").value(4))
                .andExpect(jsonPath("$.countriesWithEmployees").value(2))
                .andExpect(jsonPath("$.countries[0].countryCode").value("US"))
                .andExpect(jsonPath("$.countries[0].currency").value("USD"))
                .andExpect(jsonPath("$.countries[0].salary.headcount").value(3))
                .andExpect(jsonPath("$.countries[0].salary.min").value(90_000))
                .andExpect(jsonPath("$.countries[0].salary.max").value(140_000))
                .andExpect(jsonPath("$.countries[0].salary.average").value(110_000))
                .andExpect(jsonPath("$.countries[0].salary.median").value(100_000));
    }

    @Test
    void countryDetailAnswersAverageSalaryForJobTitleInCountry() throws Exception {
        mvc.perform(get("/api/insights/countries/us"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byJobTitle[1].name").value("Software Engineer"))
                .andExpect(jsonPath("$.byJobTitle[1].salary.average").value(120_000))
                .andExpect(jsonPath("$.byDepartment[0].name").value("Engineering"))
                .andExpect(jsonPath("$.byDepartment[0].salary.headcount").value(2));
    }

    @Test
    void unknownCountryIs404() throws Exception {
        mvc.perform(get("/api/insights/countries/ZZ")).andExpect(status().isNotFound());
    }

    @Test
    void referenceDataListsCountriesAndValuesInUse() throws Exception {
        mvc.perform(get("/api/reference-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries.length()").value(10))
                .andExpect(jsonPath("$.countries[0].name").value("Australia"))
                .andExpect(jsonPath("$.departments").value(org.hamcrest.Matchers.contains("Engineering", "Sales")))
                .andExpect(jsonPath("$.jobTitles").value(
                        org.hamcrest.Matchers.contains("Account Executive", "Software Engineer")));
    }

    private void save(String email, String jobTitle, String department, String countryCode, long salary) {
        employees.save(new Employee("Test " + email, email, jobTitle, department,
                countries.findById(countryCode).orElseThrow(), salary, LocalDate.of(2020, 1, 1)));
    }
}
