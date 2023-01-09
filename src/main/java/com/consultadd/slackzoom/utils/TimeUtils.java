package com.consultadd.slackzoom.utils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
            .withZone(ZoneId.of("-05:00"));

    private TimeUtils() {
    }

    public static String timeToString(LocalTime time) {
        return time.format(timeFormatter);
    }

    public static LocalTime stringToLocalTime(String time) {
        return LocalTime.parse(time, timeFormatter);
    }
}
