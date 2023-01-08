package com.consultadd.slackzoom.utils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm a")
            .withZone(ZoneId.of("-05:00"));
    public static String timeToString(LocalTime time){
        return time.format(timeFormatter);
    }
}
