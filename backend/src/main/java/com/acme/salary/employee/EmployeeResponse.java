package com.acme.salary.employee;

import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        String fullName,
        String email,
        String jobTitle,
        String department,
        String countryCode,
        String countryName,
        String currency,
        long annualSalary,
        LocalDate hireDate) {

    static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getJobTitle(),
                employee.getDepartment(),
                employee.getCountry().getCode(),
                employee.getCountry().getName(),
                employee.getCountry().getCurrency(),
                employee.getAnnualSalary(),
                employee.getHireDate());
    }
}
