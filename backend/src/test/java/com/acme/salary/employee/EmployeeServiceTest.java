package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.acme.salary.common.FieldValidationException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.country.Country;
import com.acme.salary.country.CountryRepository;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private static final Country INDIA = new Country("IN", "India", "INR");

    @Mock
    private EmployeeRepository employees;
    @Mock
    private CountryRepository countries;

    private EmployeeService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeService(employees, countries);
    }

    @Test
    void createTrimsInputLowercasesEmailAndDerivesCurrencyFromCountry() {
        when(countries.findById("IN")).thenReturn(Optional.of(INDIA));
        when(employees.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeResponse created = service.create(
                request("  Asha Rao ", " Asha.Rao@ACME.com ", " in ", 1_800_000));

        ArgumentCaptor<Employee> saved = ArgumentCaptor.forClass(Employee.class);
        verify(employees).save(saved.capture());
        assertThat(saved.getValue().getFullName()).isEqualTo("Asha Rao");
        assertThat(saved.getValue().getEmail()).isEqualTo("asha.rao@acme.com");
        assertThat(created.countryCode()).isEqualTo("IN");
        assertThat(created.currency()).isEqualTo("INR");
        assertThat(created.annualSalary()).isEqualTo(1_800_000);
    }

    @Test
    void createRejectsEmailAlreadyInUseRegardlessOfCase() {
        when(employees.existsByEmail("asha.rao@acme.com")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("Asha Rao", "ASHA.RAO@acme.com", "IN", 1_000)))
                .isInstanceOfSatisfying(FieldValidationException.class, ex -> {
                    assertThat(ex.getField()).isEqualTo("email");
                    assertThat(ex.isConflict()).isTrue();
                });
        verify(employees, never()).save(any());
    }

    @Test
    void createRejectsUnknownCountry() {
        when(countries.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request("Asha Rao", "asha@acme.com", "XX", 1_000)))
                .isInstanceOfSatisfying(FieldValidationException.class, ex -> {
                    assertThat(ex.getField()).isEqualTo("countryCode");
                    assertThat(ex.isConflict()).isFalse();
                });
    }

    @Test
    void updateAllowsKeepingOwnEmailAndAppliesChanges() {
        Employee existing = new Employee("Asha Rao", "asha@acme.com", "Engineer", "Engineering", INDIA,
                1_000_000, LocalDate.of(2020, 1, 1));
        when(employees.findById(7L)).thenReturn(Optional.of(existing));
        when(employees.existsByEmailAndIdNot("asha@acme.com", 7L)).thenReturn(false);
        when(countries.findById("IN")).thenReturn(Optional.of(INDIA));

        EmployeeResponse updated = service.update(7L, request("Asha Rao", "asha@acme.com", "IN", 1_250_000));

        assertThat(updated.annualSalary()).isEqualTo(1_250_000);
        assertThat(existing.getAnnualSalary()).isEqualTo(1_250_000);
    }

    @Test
    void updateRejectsEmailOwnedByAnotherEmployee() {
        when(employees.findById(7L)).thenReturn(Optional.of(new Employee("A", "a@acme.com", "T", "D", INDIA, 1,
                LocalDate.of(2020, 1, 1))));
        when(employees.existsByEmailAndIdNot("taken@acme.com", 7L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(7L, request("A", "taken@acme.com", "IN", 1)))
                .isInstanceOf(FieldValidationException.class);
    }

    @Test
    void getAndDeleteFailWithNotFoundForUnknownId() {
        when(employees.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(NotFoundException.class);
    }

    private static EmployeeRequest request(String name, String email, String countryCode, long salary) {
        return new EmployeeRequest(name, email, " Software Engineer ", " Engineering ", countryCode, salary,
                LocalDate.of(2021, 3, 15));
    }
}
