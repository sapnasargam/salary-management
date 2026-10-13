package com.acme.salary.referencedata;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.salary.country.CountryRepository;
import com.acme.salary.employee.EmployeeRepository;

/** Values for UI dropdowns and filters: all countries, plus departments and job titles currently in use. */
@RestController
public class ReferenceDataController {

    public record CountryOption(String code, String name, String currency) {
    }

    public record ReferenceData(List<CountryOption> countries, List<String> departments, List<String> jobTitles) {
    }

    private final CountryRepository countries;
    private final EmployeeRepository employees;

    public ReferenceDataController(CountryRepository countries, EmployeeRepository employees) {
        this.countries = countries;
        this.employees = employees;
    }

    @GetMapping("/api/reference-data")
    @Transactional(readOnly = true)
    public ReferenceData referenceData() {
        List<CountryOption> countryOptions = countries.findAllByOrderByNameAsc().stream()
                .map(c -> new CountryOption(c.getCode(), c.getName(), c.getCurrency()))
                .toList();
        return new ReferenceData(countryOptions, employees.findDistinctDepartments(), employees.findDistinctJobTitles());
    }
}
