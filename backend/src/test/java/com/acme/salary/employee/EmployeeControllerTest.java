package com.acme.salary.employee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.acme.salary.common.FieldValidationException;
import com.acme.salary.common.NotFoundException;

/** HTTP contract of the employee API: status codes, validation errors and query parameter handling. */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final EmployeeResponse ASHA = new EmployeeResponse(1L, "Asha Rao", "asha@acme.com",
            "Software Engineer", "Engineering", "IN", "India", "INR", 1_800_000, LocalDate.of(2021, 3, 15));

    @Autowired
    private MockMvc mvc;

    @MockBean
    private EmployeeService service;

    @Test
    void searchPassesFiltersPagingAndWhitelistedSortToService() throws Exception {
        when(service.search(any(), any())).thenReturn(new PageImpl<>(List.of(ASHA), PageRequest.of(2, 10), 21));

        mvc.perform(get("/api/employees")
                        .param("search", "asha").param("country", "IN")
                        .param("page", "2").param("size", "10")
                        .param("sort", "country").param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].currency").value("INR"))
                .andExpect(jsonPath("$.totalElements").value(21))
                .andExpect(jsonPath("$.totalPages").value(3));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).search(eq(new EmployeeSearchCriteria("asha", "IN", null, null)), pageable.capture());
        org.assertj.core.api.Assertions.assertThat(pageable.getValue().getSort())
                .containsExactly(Sort.Order.desc("country.name"), Sort.Order.asc("id"));
    }

    @Test
    void searchRejectsUnknownSortColumn() throws Exception {
        mvc.perform(get("/api/employees").param("sort", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sort").exists());
        verifyNoInteractions(service);
    }

    @Test
    void searchRejectsOversizedPage() throws Exception {
        mvc.perform(get("/api/employees").param("size", "5000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.size").exists());
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(ASHA);

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employees/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createReportsEveryInvalidFieldAndSkipsService() throws Exception {
        String json = """
                {"fullName":" ","email":"not-an-email","jobTitle":"Engineer","department":"Engineering",
                 "countryCode":"India","annualSalary":-5,"hireDate":"2999-01-01"}
                """;

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.countryCode").exists())
                .andExpect(jsonPath("$.errors.annualSalary").exists())
                .andExpect(jsonPath("$.errors.hireDate").exists());
        verifyNoInteractions(service);
    }

    @Test
    void duplicateEmailIsA409WithFieldError() throws Exception {
        when(service.create(any())).thenThrow(FieldValidationException.conflict("email", "already used"));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(validJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.email").value("already used"));
    }

    @Test
    void unknownEmployeeIs404Problem() throws Exception {
        when(service.get(42L)).thenThrow(new NotFoundException("Employee 42 not found"));

        mvc.perform(get("/api/employees/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Employee 42 not found"));
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest());
    }

    private static String validJson() {
        return """
                {"fullName":"Asha Rao","email":"asha@acme.com","jobTitle":"Software Engineer",
                 "department":"Engineering","countryCode":"IN","annualSalary":1800000,"hireDate":"2021-03-15"}
                """;
    }
}
