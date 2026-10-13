package com.acme.salary.seed;

import java.time.LocalDate;

/** A generated employee row, ready to be inserted. */
public record SeedEmployee(String fullName, String email, String jobTitle, String department, String countryCode,
                           long annualSalary, LocalDate hireDate) {
}
