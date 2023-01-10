package com.consultadd.slackzoom.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static String ZONE_ID = "-05:00";
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
            .withZone(ZoneId.of(ZONE_ID));

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd")
            .withZone(ZoneId.of(ZONE_ID));

    private DateTimeUtils() {
    }

    public static String timeToString(LocalTime time) {
        return time.format(timeFormatter);
    }

    public static LocalTime stringToLocalTime(String time) {
        return LocalTime.parse(time, timeFormatter);
    }

    public static LocalDate stringToDate(String date) {
        return LocalDate.parse(date, dateTimeFormatter);
    }

    public static String dateToString(LocalDate date) {
        return date.format(dateTimeFormatter);
    }
}
