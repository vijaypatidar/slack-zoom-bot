package com.consultadd.slackzoom.models;

import com.consultadd.slackzoom.services.AccountType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

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

    public static Account toZoomAccount(Map<String, AttributeValue> valueMap) {
        return Account
                .builder()
                .accountId(valueMap.get("account_id").s())
                .username(valueMap.get("username").s())
                .password(valueMap.get("password").s())
                .accountName(valueMap.get("account_name").s())
                .accountType(AccountType.ZOOM)
                .build();
    }
}
