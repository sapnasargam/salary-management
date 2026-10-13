package com.acme.salary.employee;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

/**
 * Composable filters for the employee directory. Each returns {@code null} for a blank input,
 * which Spring Data treats as "no restriction", so callers can combine them unconditionally.
 */
final class EmployeeSpecifications {

    private EmployeeSpecifications() {
    }

    static Specification<Employee> matching(EmployeeSearchCriteria criteria) {
        return Specification.where(nameOrEmailContains(criteria.search()))
                .and(inCountry(criteria.country()))
                .and(hasDepartment(criteria.department()))
                .and(hasJobTitle(criteria.jobTitle()));
    }

    static Specification<Employee> nameOrEmailContains(String text) {
        if (isBlank(text)) {
            return null;
        }
        String pattern = "%" + escapeLike(text.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("fullName")), pattern, '\\'),
                cb.like(cb.lower(root.get("email")), pattern, '\\'));
    }

    static Specification<Employee> inCountry(String countryCode) {
        if (isBlank(countryCode)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("country").get("code"), countryCode.trim().toUpperCase(Locale.ROOT));
    }

    static Specification<Employee> hasDepartment(String department) {
        if (isBlank(department)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("department"), department.trim());
    }

    static Specification<Employee> hasJobTitle(String jobTitle) {
        if (isBlank(jobTitle)) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("jobTitle"), jobTitle.trim());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
