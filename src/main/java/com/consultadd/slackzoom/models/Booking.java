package com.consultadd.slackzoom.models;

import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Booking {
    @Nonnull
    LocalTime startTime;
    @Nonnull
    LocalTime endTime;
    @Nonnull
    String userId;
    @Nonnull
    String bookingId;
    @Nonnull
    String accountId;
    @Nonnull
    LocalDate bookingDate;
}