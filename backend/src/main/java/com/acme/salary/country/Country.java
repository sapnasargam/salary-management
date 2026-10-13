package com.acme.salary.country;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Reference data: a country ACME employs people in, and the currency salaries there are paid in.
 * Managed by Flyway migrations, read-only for the application.
 */
@Entity
@Table(name = "country")
public class Country {

    @Id
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String currency;

    protected Country() {
        // for JPA
    }

    public Country(String code, String name, String currency) {
        this.code = code;
        this.name = name;
        this.currency = currency;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }
}
