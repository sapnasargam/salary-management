package com.acme.salary.insights;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.acme.salary.employee.Employee;

/**
 * Read-only projection queries for analytics. Loading four columns for 10k rows is cheap and lets the
 * statistics (including the median, which SQLite cannot compute) live in plain, unit-testable Java.
 */
public interface SalaryRecordRepository extends Repository<Employee, Long> {

    @Query("""
            select new com.acme.salary.insights.SalaryRecord(e.country.code, e.department, e.jobTitle, e.annualSalary)
            from Employee e
            """)
    List<SalaryRecord> findAllRecords();

    @Query("""
            select new com.acme.salary.insights.SalaryRecord(e.country.code, e.department, e.jobTitle, e.annualSalary)
            from Employee e
            where e.country.code = :countryCode
            """)
    List<SalaryRecord> findRecordsByCountry(@Param("countryCode") String countryCode);
}
