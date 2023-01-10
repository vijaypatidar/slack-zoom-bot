package com.consultadd.slackzoom.models;

import com.consultadd.slackzoom.enums.AccountType;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingRequest {
    LocalTime startTime;
    LocalTime endTime;
    String userId;
    LocalDate bookingDate;
    AccountType accountType;
}