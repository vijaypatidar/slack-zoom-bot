package com.consultadd.slackzoom.models;

import com.consultadd.slackzoom.enums.AccountType;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Account {
    @Nonnull
    private String accountName;
    @Nonnull
    private String username;
    @Nonnull
    private String password;
    @Nonnull
    private String accountId;
    @Nonnull
    private AccountType accountType;
}
