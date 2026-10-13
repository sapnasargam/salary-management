package com.acme.salary.insights;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.salary.common.NotFoundException;
import com.acme.salary.country.Country;
import com.acme.salary.country.CountryRepository;
import com.acme.salary.insights.InsightsResponses.CountryDetail;
import com.acme.salary.insights.InsightsResponses.CountryInsight;
import com.acme.salary.insights.InsightsResponses.GroupInsight;
import com.acme.salary.insights.InsightsResponses.OrgOverview;

/**
 * Answers "how does the org pay people?". Statistics are always computed per country, never across
 * countries, because salaries are stored in local currency and mixing currencies would be meaningless.
 */
@Service
@Transactional(readOnly = true)
public class InsightsService {

    private final SalaryRecordRepository salaryRecords;
    private final CountryRepository countries;

    public InsightsService(SalaryRecordRepository salaryRecords, CountryRepository countries) {
        this.salaryRecords = salaryRecords;
        this.countries = countries;
    }

    /** Headcount and salary statistics for every country, largest workforce first. */
    public OrgOverview overview() {
        Map<String, List<Long>> salariesByCountry = salaryRecords.findAllRecords().stream()
                .collect(groupingBy(SalaryRecord::countryCode, mapping(SalaryRecord::annualSalary, toList())));

        List<CountryInsight> insights = countries.findAllByOrderByNameAsc().stream()
                .map(country -> new CountryInsight(country.getCode(), country.getName(), country.getCurrency(),
                        SalaryStatistics.of(salariesByCountry.getOrDefault(country.getCode(), List.of()))))
                .sorted(Comparator.comparingInt((CountryInsight c) -> c.salary().headcount()).reversed())
                .toList();

        int totalHeadcount = insights.stream().mapToInt(c -> c.salary().headcount()).sum();
        int countriesWithEmployees = (int) insights.stream().filter(c -> c.salary().headcount() > 0).count();
        return new OrgOverview(totalHeadcount, countriesWithEmployees, insights);
    }

    /** Statistics for one country, broken down by job title and by department. */
    public CountryDetail countryDetail(String countryCode) {
        String code = countryCode.trim().toUpperCase(Locale.ROOT);
        Country country = countries.findById(code)
                .orElseThrow(() -> new NotFoundException("Country " + code + " not found"));
        List<SalaryRecord> records = salaryRecords.findRecordsByCountry(code);

        return new CountryDetail(country.getCode(), country.getName(), country.getCurrency(),
                SalaryStatistics.of(records.stream().map(SalaryRecord::annualSalary).toList()),
                statisticsBy(records, SalaryRecord::jobTitle),
                statisticsBy(records, SalaryRecord::department));
    }

    /** Groups records by {@code key} and summarises each group; groups are ordered alphabetically. */
    static List<GroupInsight> statisticsBy(List<SalaryRecord> records, Function<SalaryRecord, String> key) {
        Map<String, List<Long>> groups = records.stream()
                .collect(groupingBy(key, TreeMap::new, mapping(SalaryRecord::annualSalary, toList())));
        return groups.entrySet().stream()
                .map(group -> new GroupInsight(group.getKey(), SalaryStatistics.of(group.getValue())))
                .toList();
    }
}
