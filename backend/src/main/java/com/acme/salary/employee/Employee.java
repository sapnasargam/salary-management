package com.acme.salary.employee;

import java.time.LocalDate;

import com.acme.salary.country.Country;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * An employee and their current annual salary.
 * <p>
 * The salary is a whole amount in the currency of the employee's {@link Country}; there is
 * deliberately no separate currency field, so the two can never disagree.
 */
@Entity
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String department;

    @ManyToOne(optional = false)
    @JoinColumn(name = "country_code", nullable = false)
    private Country country;

    @Column(name = "annual_salary", nullable = false)
    private long annualSalary;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    protected Employee() {
        // for JPA
    }

    public Employee(String fullName, String email, String jobTitle, String department,
                    Country country, long annualSalary, LocalDate hireDate) {
        update(fullName, email, jobTitle, department, country, annualSalary, hireDate);
    }

    public void update(String fullName, String email, String jobTitle, String department,
                       Country country, long annualSalary, LocalDate hireDate) {
        this.fullName = fullName;
        this.email = email;
        this.jobTitle = jobTitle;
        this.department = department;
        this.country = country;
        this.annualSalary = annualSalary;
        this.hireDate = hireDate;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getDepartment() {
        return department;
    }

    public Country getCountry() {
        return country;
    }

    public long getAnnualSalary() {
        return annualSalary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }
}
