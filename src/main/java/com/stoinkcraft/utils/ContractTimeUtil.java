package com.stoinkcraft.utils;

import java.time.*;

public final class ContractTimeUtil {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    private ContractTimeUtil() {}

    /**
     * @return timestamp for the next daily reset (midnight)
     */
    public static long nextDay() {
        return LocalDate.now(ZONE)
                .plusDays(1)
                .atStartOfDay(ZONE)
                .toInstant()
                .toEpochMilli();
    }

    /**
     * @return timestamp for the next weekly reset (Monday midnight)
     */
    public static long nextWeek() {
        return LocalDate.now(ZONE)
                .with(DayOfWeek.MONDAY)
                .plusWeeks(1)
                .atStartOfDay(ZONE)
                .toInstant()
                .toEpochMilli();
    }
}