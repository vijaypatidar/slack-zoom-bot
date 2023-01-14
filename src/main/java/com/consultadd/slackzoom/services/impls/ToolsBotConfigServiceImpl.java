package com.consultadd.slackzoom.services.impls;

import com.consultadd.slackzoom.services.ToolsBotConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@RequiredArgsConstructor
public class ToolsBotConfigServiceImpl extends AbstractDynamoDbService<Map<String, String>> implements ToolsBotConfigService {
    public static final String CHANNEL_ID = "channelId";
    public static final String TIME_STAMP = "ts";

    private final ObjectMapper objectMapper;
    @Value(value = "${DB_TOOLS_BOT_CONFIG_TABLE_NAME}")
    String toolsBotConfigTableName;

    @Override
    public Optional<String> getTsForChannel(String channelId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(CHANNEL_ID, AttributeValue.builder().s(channelId).build());
        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(toolsBotConfigTableName)
                .key(key).build();
        GetItemResponse item = getDynamoDbClient().getItem(getItemRequest);
        if (item.hasItem()) {
            return Optional.ofNullable(item.item().get(TIME_STAMP).s());
        }
        return Optional.empty();
    }

    @Override
    public void saveTsForChannel(String channelId, String ts) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(CHANNEL_ID, AttributeValue.builder().s(channelId).build());
        item.put(TIME_STAMP, AttributeValue.builder().s(ts).build());
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(toolsBotConfigTableName)
                .item(item).build();
        getDynamoDbClient().putItem(putItemRequest);
    }

    @Override
    protected String getTableName() {
        return this.toolsBotConfigTableName;
    }

    @Override
    protected Map<String, String> toModal(Map<String, AttributeValue> item) {
        return Map.of();
    }

    @Override
    protected Map<String, AttributeValue> toItem(Map<String, String> modal) {
        return Map.of();
    }
}
