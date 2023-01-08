package com.consultadd.slackzoom.models;

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
public class ZoomAccount {
    private String accountName;
    private String username;
    private String password;
    private String accountId;

    public static ZoomAccount toZoomAccount(Map<String, AttributeValue> valueMap) {
        return ZoomAccount
                .builder()
                .accountId(valueMap.get("account_id").s())
                .username(valueMap.get("username").s())
                .password(valueMap.get("password").s())
                .accountName(valueMap.get("account_name").s())
                .build();
    }
}
