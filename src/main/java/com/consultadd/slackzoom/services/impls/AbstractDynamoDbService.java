package com.consultadd.slackzoom.services.impls;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public abstract class AbstractDynamoDbService<T> {
    private DynamoDbClient dynamoDbClient;

    public DynamoDbClient getDynamoDbClient() {
        return dynamoDbClient;
    }

    @Autowired
    public void setDynamoDbClient(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    protected abstract String getTableName();

    protected Optional<T> getItem(Map<String, AttributeValue> key) {
        GetItemRequest getItemRequest = GetItemRequest
                .builder()
                .tableName(getTableName())
                .key(key)
                .build();
        GetItemResponse itemResponse = getDynamoDbClient().getItem(getItemRequest);
        if (itemResponse.hasItem()) {
            return Optional.of(toModal(itemResponse.item()));
        } else {
            return Optional.empty();
        }
    }

    protected List<T> scan(String filterExpression, Map<String, AttributeValue> expressionAttributeValues) {
        return getDynamoDbClient().scan(ScanRequest.builder()
                .filterExpression(filterExpression)
                .expressionAttributeValues(expressionAttributeValues)
                .tableName(getTableName()).build())
                .items().stream().map(this::toModal).toList();
    }

    protected PutItemResponse putItem(T modal) {
        return getDynamoDbClient().putItem(PutItemRequest
                .builder()
                .item(toItem(modal))
                .tableName(getTableName())
                .build());
    }

    protected DeleteItemResponse deleteItem(Map<String,AttributeValue> key){
        DeleteItemRequest deleteItemRequest = DeleteItemRequest
                .builder()
                .tableName(getTableName())
                .key(key)
                .build();
        return getDynamoDbClient().deleteItem(deleteItemRequest);
    }

    protected abstract T toModal(Map<String, AttributeValue> item);

    protected abstract Map<String, AttributeValue> toItem(T modal);
}
