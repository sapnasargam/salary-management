package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.acme.salary.common.NotFoundException;
import com.acme.salary.country.Country;
import com.acme.salary.country.CountryRepository;
import com.acme.salary.insights.InsightsResponses.CountryDetail;
import com.acme.salary.insights.InsightsResponses.CountryInsight;
import com.acme.salary.insights.InsightsResponses.GroupInsight;
import com.acme.salary.insights.InsightsResponses.OrgOverview;

@ExtendWith(MockitoExtension.class)
class InsightsServiceTest {

    private static final Country INDIA = new Country("IN", "India", "INR");
    private static final Country JAPAN = new Country("JP", "Japan", "JPY");
    private static final Country USA = new Country("US", "United States", "USD");

    @Mock
    private SalaryRecordRepository salaryRecords;
    @Mock
    private CountryRepository countries;

    private InsightsService service;

    @BeforeEach
    void setUp() {
        service = new InsightsService(salaryRecords, countries);
    }

    @Test
    void overviewComputesStatisticsPerCountryNeverMixingCurrencies() {
        when(countries.findAllByOrderByNameAsc()).thenReturn(List.of(INDIA, JAPAN, USA));
        when(salaryRecords.findAllRecords()).thenReturn(List.of(
                record("US", "Eng", "Engineer", 100_000),
                record("US", "Eng", "Engineer", 140_000),
                record("IN", "Eng", "Engineer", 2_000_000),
                record("US", "Sales", "Account Executive", 90_000)));

        OrgOverview overview = service.overview();

        assertThat(overview.totalHeadcount()).isEqualTo(4);
        assertThat(overview.countriesWithEmployees()).isEqualTo(2);
        assertThat(overview.countries()).extracting(CountryInsight::countryCode)
                .as("largest workforce first, empty countries still listed")
                .containsExactly("US", "IN", "JP");

        CountryInsight us = overview.countries().get(0);
        assertThat(us.currency()).isEqualTo("USD");
        assertThat(us.salary()).isEqualTo(new SalaryStatistics(3, 90_000, 140_000, 110_000, 100_000));
        assertThat(overview.countries().get(2).salary()).isEqualTo(SalaryStatistics.EMPTY);
    }

    @Test
    void countryDetailBreaksDownByJobTitleAndDepartmentAlphabetically() {
        when(countries.findById("US")).thenReturn(Optional.of(USA));
        when(salaryRecords.findRecordsByCountry("US")).thenReturn(List.of(
                record("US", "Engineering", "Software Engineer", 100_000),
                record("US", "Engineering", "Software Engineer", 120_000),
                record("US", "Engineering", "Engineering Manager", 180_000),
                record("US", "Sales", "Account Executive", 80_000)));

        CountryDetail detail = service.countryDetail(" us ");

        assertThat(detail.currency()).isEqualTo("USD");
        assertThat(detail.salary().headcount()).isEqualTo(4);
        assertThat(detail.byJobTitle()).containsExactly(
                new GroupInsight("Account Executive", new SalaryStatistics(1, 80_000, 80_000, 80_000, 80_000)),
                new GroupInsight("Engineering Manager", new SalaryStatistics(1, 180_000, 180_000, 180_000, 180_000)),
                new GroupInsight("Software Engineer", new SalaryStatistics(2, 100_000, 120_000, 110_000, 110_000)));
        assertThat(detail.byDepartment()).extracting(GroupInsight::name).containsExactly("Engineering", "Sales");
        assertThat(detail.byDepartment().get(0).salary().headcount()).isEqualTo(3);
    }

    @Test
    void countryDetailForCountryWithoutEmployeesIsEmptyNotAnError() {
        when(countries.findById("JP")).thenReturn(Optional.of(JAPAN));
        when(salaryRecords.findRecordsByCountry("JP")).thenReturn(List.of());

        CountryDetail detail = service.countryDetail("JP");

        assertThat(detail.salary()).isEqualTo(SalaryStatistics.EMPTY);
        assertThat(detail.byJobTitle()).isEmpty();
    }

    @Test
    void countryDetailForUnknownCountryIsNotFound() {
        when(countries.findById("XX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.countryDetail("xx")).isInstanceOf(NotFoundException.class);
    }

    private static SalaryRecord record(String country, String department, String jobTitle, long salary) {
        return new SalaryRecord(country, department, jobTitle, salary);
    }
}
