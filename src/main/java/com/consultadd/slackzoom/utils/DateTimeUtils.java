package com.consultadd.slackzoom.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final ZoneId ZONE_ID = ZoneId.of("-05:00");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZONE_ID);
    private static final DateTimeFormatter time24Formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZONE_ID);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd").withZone(ZONE_ID);

    private DateTimeUtils() {
    }

    public static String timeToString(LocalTime time) {
        return time.format(timeFormatter).toUpperCase();
    }

    public static LocalTime stringToLocalTime(String time) {
        if (time.length()>5){
            return LocalTime.parse(time,timeFormatter);
        }
        return LocalTime.parse(time, time24Formatter);
    }


    public static LocalDate stringToDate(String date) {
        return LocalDate.parse(date, dateTimeFormatter);
    }

    public static String dateToString(LocalDate date) {
        return date.format(dateTimeFormatter);
    }
}
