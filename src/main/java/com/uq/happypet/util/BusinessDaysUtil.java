package com.uq.happypet.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class BusinessDaysUtil {

    private BusinessDaysUtil() {
    }

    public static LocalDate addBusinessDays(LocalDate start, int businessDays) {
        if (businessDays <= 0) {
            return start;
        }
        LocalDate d = start;
        int added = 0;
        while (added < businessDays) {
            d = d.plusDays(1);
            if (!isWeekend(d)) {
                added++;
            }
        }
        return d;
    }

    public static boolean isWeekend(LocalDate d) {
        DayOfWeek w = d.getDayOfWeek();
        return w == DayOfWeek.SATURDAY || w == DayOfWeek.SUNDAY;
    }

    public static boolean isWithinBusinessDayRange(LocalDate chosen, LocalDate minInclusive, LocalDate maxInclusive) {
        if (chosen == null || minInclusive == null || maxInclusive == null) {
            return false;
        }
        if (chosen.isBefore(minInclusive) || chosen.isAfter(maxInclusive)) {
            return false;
        }
        return !isWeekend(chosen);
    }

    /**
     * Business days between {@code from} and {@code to} (inclusive), ordered.
     */
    public static List<LocalDate> listBusinessDaysInclusive(LocalDate from, LocalDate to) {
        List<LocalDate> out = new ArrayList<>();
        if (from == null || to == null || from.isAfter(to)) {
            return out;
        }
        LocalDate d = from;
        while (!d.isAfter(to)) {
            if (!isWeekend(d)) {
                out.add(d);
            }
            d = d.plusDays(1);
        }
        return out;
    }
}