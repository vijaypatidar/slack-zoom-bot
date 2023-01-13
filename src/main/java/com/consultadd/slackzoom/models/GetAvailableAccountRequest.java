package com.consultadd.slackzoom.models;

import com.consultadd.slackzoom.enums.AccountType;
import jakarta.annotation.Nonnull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetAvailableAccountRequest {
    @Nonnull
    private LocalTime startTime;
    @Nonnull
    private LocalTime endTime;
    @Nonnull
    private AccountType accountType;
    @Nonnull
    private LocalDate bookingDate;
}
