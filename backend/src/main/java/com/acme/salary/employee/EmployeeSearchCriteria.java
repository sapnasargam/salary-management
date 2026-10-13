package com.acme.salary.employee;

/** Optional filters for the employee directory; any of them may be null or blank. */
public record EmployeeSearchCriteria(String search, String country, String department, String jobTitle) {
}
