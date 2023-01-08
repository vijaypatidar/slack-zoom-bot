package com.consultadd.slackzoom.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    int startTime;
    int endTime;
    String userId;
    String bookingId;
    String accountId;
}