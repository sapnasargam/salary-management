package com.acme.salary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.acme.salary.country.CountryRepository;

@SpringBootTest
@ActiveProfiles("test")
class SalaryManagementApplicationTests {

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void migrationsCreateSchemaAndReferenceCountries() {
        assertThat(countryRepository.findById("IN"))
                .hasValueSatisfying(country -> assertThat(country.getCurrency()).isEqualTo("INR"));
        assertThat(countryRepository.count()).isEqualTo(10);
    }
}
