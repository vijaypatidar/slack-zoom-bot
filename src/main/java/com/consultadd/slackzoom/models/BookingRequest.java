package com.consultadd.slackzoom.models;

import com.consultadd.slackzoom.enums.AccountType;
import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingRequest {
    @Nonnull
    LocalTime startTime;
    @Nonnull
    LocalTime endTime;
    @Nonnull
    String userId;
    @Nonnull
    LocalDate bookingDate;
    @Nonnull
    AccountType accountType;
}