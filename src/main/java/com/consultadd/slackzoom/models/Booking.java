package com.consultadd.slackzoom.models;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {
    LocalTime startTime;
    LocalTime endTime;
    String userId;
    String bookingId;
    String accountId;
}