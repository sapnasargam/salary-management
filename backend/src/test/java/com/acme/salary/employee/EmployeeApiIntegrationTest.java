package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** End-to-end through HTTP, service, JPA and a real SQLite database (dates, joins, LIKE, sorting). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeApiIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;
    @Autowired
    private EmployeeRepository employees;

    @BeforeEach
    void cleanDatabase() {
        employees.deleteAll();
    }

    @Test
    void employeeLifecycle() throws Exception {
        long id = create("Asha Rao", "asha@acme.com", "IN", 1_800_000);

        mvc.perform(get("/api/employees/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countryName").value("India"))
                .andExpect(jsonPath("$.currency").value("INR"))
                .andExpect(jsonPath("$.hireDate").value("2021-03-15"));

        mvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Asha Rao", "asha@acme.com", "DE", 95_000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.annualSalary").value(95_000));

        mvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/employees/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void emailUniquenessIsCaseInsensitive() throws Exception {
        create("Asha Rao", "asha@acme.com", "IN", 1_800_000);

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Other Asha", "ASHA@ACME.COM", "US", 100_000)))
                .andExpect(status().isConflict());
    }

    @Test
    void searchCombinesTextAndCountryFiltersAndSortsBySalary() throws Exception {
        create("Asha Rao", "asha@acme.com", "IN", 1_800_000);
        create("Ashley Cole", "ashley@acme.com", "US", 120_000);
        create("Ashton Hall", "ashton@acme.com", "US", 150_000);
        create("Bob Stone", "bob@acme.com", "US", 90_000);

        mvc.perform(get("/api/employees")
                        .param("search", "ASH").param("country", "us")
                        .param("sort", "annualSalary").param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].fullName").value("Ashton Hall"))
                .andExpect(jsonPath("$.content[1].fullName").value("Ashley Cole"));
    }

    @Test
    void searchTreatsLikeWildcardsLiterally() throws Exception {
        create("Percy Smith", "percy@acme.com", "US", 100_000);

        mvc.perform(get("/api/employees").param("search", "%"))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void unknownCountryIsRejected() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Asha Rao", "asha@acme.com", "ZZ", 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.countryCode").exists());
        assertThat(employees.count()).isZero();
    }

    private long create(String name, String email, String country, long salary) throws Exception {
        String response = mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(body(name, email, country, salary)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(response);
        return node.get("id").asLong();
    }

    private String body(String name, String email, String country, long salary) throws Exception {
        return json.writeValueAsString(new EmployeeRequest(name, email, "Software Engineer", "Engineering",
                country, salary, java.time.LocalDate.of(2021, 3, 15)));
    }
}
