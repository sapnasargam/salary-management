package com.acme.salary.insights;

import java.util.Arrays;
import java.util.Collection;

/**
 * Summary statistics for a group of salaries that share one currency.
 * <p>
 * Average and median are rounded to whole currency units (half-up), matching how salaries are stored.
 * The median is reported alongside the average because a few very high salaries skew the average,
 * and HR usually asks "what does a typical person earn?".
 *
 * @param headcount number of salaries in the group; when 0 every other value is 0
 */
public record SalaryStatistics(int headcount, long min, long max, long average, long median) {

    public static final SalaryStatistics EMPTY = new SalaryStatistics(0, 0, 0, 0, 0);

    public static SalaryStatistics of(Collection<Long> salaries) {
        return of(salaries.stream().mapToLong(Long::longValue).toArray());
    }

    public static SalaryStatistics of(long[] salaries) {
        if (salaries.length == 0) {
            return EMPTY;
        }
        long[] sorted = salaries.clone();
        Arrays.sort(sorted);

        long sum = 0;
        for (long salary : sorted) {
            sum = Math.addExact(sum, salary);
        }
        int n = sorted.length;
        long median = n % 2 == 1
                ? sorted[n / 2]
                : roundHalfUp(sorted[n / 2 - 1] + sorted[n / 2], 2);

        return new SalaryStatistics(n, sorted[0], sorted[n - 1], roundHalfUp(sum, n), median);
    }

    /** Integer division rounded half-up, for non-negative values (salaries are always positive). */
    private static long roundHalfUp(long dividend, long divisor) {
        return (dividend + divisor / 2) / divisor;
    }
}
