package com.acme.salary.employee;

import java.util.Arrays;

import org.springframework.data.domain.Sort;

import com.acme.salary.common.FieldValidationException;

/**
 * The columns the directory can be sorted by. An explicit allow-list keeps API sort keys decoupled
 * from entity property paths and turns unknown keys into a clean 400 instead of a query error.
 */
enum EmployeeSortField {
    FULL_NAME("fullName", "fullName"),
    EMAIL("email", "email"),
    JOB_TITLE("jobTitle", "jobTitle"),
    DEPARTMENT("department", "department"),
    COUNTRY("country", "country.name"),
    ANNUAL_SALARY("annualSalary", "annualSalary"),
    HIRE_DATE("hireDate", "hireDate");

    private final String apiName;
    private final String property;

    EmployeeSortField(String apiName, String property) {
        this.apiName = apiName;
        this.property = property;
    }

    static EmployeeSortField fromApiName(String apiName) {
        return Arrays.stream(values())
                .filter(field -> field.apiName.equals(apiName))
                .findFirst()
                .orElseThrow(() -> FieldValidationException.invalid("sort", "Cannot sort by '" + apiName + "'"));
    }

    /** Sorts by this field, with id as a tie-breaker so pagination is stable across pages. */
    Sort toSort(Sort.Direction direction) {
        return Sort.by(direction, property).and(Sort.by(Sort.Direction.ASC, "id"));
    }
}
