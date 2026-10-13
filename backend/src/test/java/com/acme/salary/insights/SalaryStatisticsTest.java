package com.acme.salary.insights;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SalaryStatisticsTest {

    @Test
    void emptyGroupHasZeroHeadcountAndZeroValues() {
        assertThat(SalaryStatistics.of(List.of())).isEqualTo(SalaryStatistics.EMPTY);
    }

    @Test
    void singleSalaryIsMinMaxAverageAndMedian() {
        assertThat(SalaryStatistics.of(List.of(50_000L)))
                .isEqualTo(new SalaryStatistics(1, 50_000, 50_000, 50_000, 50_000));
    }

    @Test
    void oddCountMedianIsMiddleValueRegardlessOfInputOrder() {
        SalaryStatistics stats = SalaryStatistics.of(List.of(90L, 10L, 30L));

        assertThat(stats.min()).isEqualTo(10);
        assertThat(stats.max()).isEqualTo(90);
        assertThat(stats.median()).isEqualTo(30);
        assertThat(stats.average()).isEqualTo(43); // 130 / 3 = 43.33
    }

    @Test
    void evenCountMedianIsMeanOfTwoMiddleValuesRoundedHalfUp() {
        assertThat(SalaryStatistics.of(List.of(10L, 20L, 31L, 40L)).median()).isEqualTo(26); // 25.5 -> 26
        assertThat(SalaryStatistics.of(List.of(10L, 20L, 30L, 40L)).median()).isEqualTo(25);
    }

    @Test
    void averageRoundsHalfUp() {
        assertThat(SalaryStatistics.of(List.of(1L, 2L)).average()).isEqualTo(2);   // 1.5 -> 2
        assertThat(SalaryStatistics.of(List.of(1L, 1L, 2L)).average()).isEqualTo(1); // 1.33 -> 1
    }

    @Test
    void medianResistsOutliersWhileAverageDoesNot() {
        SalaryStatistics stats = SalaryStatistics.of(List.of(50_000L, 52_000L, 55_000L, 1_000_000L));

        assertThat(stats.median()).isEqualTo(53_500);
        assertThat(stats.average()).isEqualTo(289_250);
    }

    @Test
    void doesNotMutateCallerArray() {
        long[] salaries = {3, 1, 2};
        SalaryStatistics.of(salaries);
        assertThat(salaries).containsExactly(3, 1, 2);
    }
}
