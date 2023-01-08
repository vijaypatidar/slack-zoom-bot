package com.consultadd.slackzoom.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ZoomAccount {
    private String accountName;
    private String username;
    private String password;
    private String accountId;
}
