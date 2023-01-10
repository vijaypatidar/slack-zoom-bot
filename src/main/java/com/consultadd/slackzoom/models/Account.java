package com.consultadd.slackzoom.models;

import com.consultadd.slackzoom.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Account {
    private String accountName;
    private String username;
    private String password;
    private String accountId;
    private AccountType accountType;
}
