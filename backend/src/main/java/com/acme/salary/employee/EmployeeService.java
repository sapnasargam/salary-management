package com.acme.salary.employee;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.salary.common.FieldValidationException;
import com.acme.salary.common.NotFoundException;
import com.acme.salary.country.Country;
import com.acme.salary.country.CountryRepository;

/**
 * Use cases for maintaining employee records. Owns the business rules that bean validation
 * cannot express: the country must exist, and e-mail addresses are unique (case-insensitively).
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employees;
    private final CountryRepository countries;

    public EmployeeService(EmployeeRepository employees, CountryRepository countries) {
        this.employees = employees;
        this.countries = countries;
    }

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(EmployeeSearchCriteria criteria, Pageable pageable) {
        return employees.findAll(EmployeeSpecifications.matching(criteria), pageable).map(EmployeeResponse::from);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(long id) {
        return EmployeeResponse.from(findEmployee(id));
    }

    public EmployeeResponse create(EmployeeRequest request) {
        NormalizedRequest input = normalize(request);
        if (employees.existsByEmail(input.email())) {
            throw emailInUse();
        }
        Employee employee = new Employee(input.fullName(), input.email(), input.jobTitle(), input.department(),
                resolveCountry(input.countryCode()), request.annualSalary(), request.hireDate());
        return EmployeeResponse.from(employees.save(employee));
    }

    public EmployeeResponse update(long id, EmployeeRequest request) {
        Employee employee = findEmployee(id);
        NormalizedRequest input = normalize(request);
        if (employees.existsByEmailAndIdNot(input.email(), id)) {
            throw emailInUse();
        }
        employee.update(input.fullName(), input.email(), input.jobTitle(), input.department(),
                resolveCountry(input.countryCode()), request.annualSalary(), request.hireDate());
        return EmployeeResponse.from(employee);
    }

    public void delete(long id) {
        employees.delete(findEmployee(id));
    }

    private Employee findEmployee(long id) {
        return employees.findById(id).orElseThrow(() -> new NotFoundException("Employee " + id + " not found"));
    }

    private Country resolveCountry(String countryCode) {
        return countries.findById(countryCode)
                .orElseThrow(() -> FieldValidationException.invalid("countryCode", "Unknown country: " + countryCode));
    }

    private static FieldValidationException emailInUse() {
        return FieldValidationException.conflict("email", "This e-mail is already used by another employee");
    }

    /** Trims text and canonicalises e-mail / country code so lookups and uniqueness are case-insensitive. */
    private static NormalizedRequest normalize(EmployeeRequest request) {
        return new NormalizedRequest(
                request.fullName().trim(),
                request.email().trim().toLowerCase(Locale.ROOT),
                request.jobTitle().trim(),
                request.department().trim(),
                request.countryCode().trim().toUpperCase(Locale.ROOT));
    }

    private record NormalizedRequest(String fullName, String email, String jobTitle, String department,
                                     String countryCode) {
    }
}
