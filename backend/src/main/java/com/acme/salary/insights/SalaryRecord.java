package com.acme.salary.insights;

/** The minimal slice of an employee that salary analytics needs (a JPQL projection, not an entity). */
public record SalaryRecord(String countryCode, String department, String jobTitle, long annualSalary) {
}
