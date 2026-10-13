package com.acme.salary.employee;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Payload for creating or updating an employee. Currency is implied by {@code countryCode}. */
public record EmployeeRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 100) String jobTitle,
        @NotBlank @Size(max = 100) String department,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "must be a 2-letter ISO country code") String countryCode,
        // Upper bound guards against typos (an extra zero or two); it is in local currency, so it must allow JPY/INR.
        @NotNull @Positive @Max(1_000_000_000L) Long annualSalary,
        @NotNull @PastOrPresent LocalDate hireDate) {
}
