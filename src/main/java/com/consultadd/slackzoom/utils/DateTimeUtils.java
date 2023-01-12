package com.consultadd.slackzoom.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd");

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
