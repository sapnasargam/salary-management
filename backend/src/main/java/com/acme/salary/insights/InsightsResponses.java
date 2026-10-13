package com.acme.salary.insights;

import java.util.List;

/** Response shapes for the insights API. All money values are in the {@code currency} of their country. */
public final class InsightsResponses {

    private InsightsResponses() {
    }

    public record CountryInsight(String countryCode, String countryName, String currency, SalaryStatistics salary) {
    }

    public record OrgOverview(int totalHeadcount, int countriesWithEmployees, List<CountryInsight> countries) {
    }

    public record GroupInsight(String name, SalaryStatistics salary) {
    }

    public record CountryDetail(String countryCode, String countryName, String currency, SalaryStatistics salary,
                                List<GroupInsight> byJobTitle, List<GroupInsight> byDepartment) {
    }
}
