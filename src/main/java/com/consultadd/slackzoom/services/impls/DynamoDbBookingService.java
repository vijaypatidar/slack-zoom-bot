package com.consultadd.slackzoom.services.impls;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.services.BookingService;
import com.consultadd.slackzoom.utils.TimeUtils;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbBookingService implements BookingService {

    @Value(value = "${DB_BOOKINGS_TABLE_NAME}")
    String bookingsTableName;
    private final DynamoDbClient dynamoDbClient;

    public Booking mapToBooking(Map<String, AttributeValue> valueMap) {
        return Booking.builder()
                .bookingId(valueMap.get("bookingId").s())
                .accountId(valueMap.get("accountId").s())
                .userId(valueMap.get("userId").s())
                .startTime(TimeUtils.stringToLocalTime(valueMap.get("startTime").s()))
                .endTime(TimeUtils.stringToLocalTime(valueMap.get("endTime").s()))
                .build();
    }


    @Override
    public List<Booking> getAllBookings(AccountType accountType) {
        try {
            ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName(bookingsTableName).build());
            return scanResponse
                    .items()
                    .stream()
                    .map(this::mapToBooking)
                    .toList();
        } catch (Exception e) {
            log.error("Error scanning accounts table", e);
            return List.of();
        }
    }

    @Override
    public void bookAccount(Booking booking) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("bookingId", AttributeValue.builder().s(booking.getBookingId()).build());
        item.put("accountId", AttributeValue.builder().s(booking.getAccountId()).build());
        item.put("userId", AttributeValue.builder().s(booking.getUserId()).build());
        item.put("startTime", AttributeValue.builder().s(TimeUtils.timeToString(booking.getStartTime())).build());
        item.put("endTime", AttributeValue.builder().s(TimeUtils.timeToString(booking.getEndTime())).build());
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .item(item)
                .tableName(bookingsTableName)
                .build();
        dynamoDbClient.putItem(putItemRequest);
    }

    @Override
    public void deleteBooking(String bookingId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("bookingId", AttributeValue.builder().s(bookingId).build());
        DeleteItemRequest deleteItemRequest = DeleteItemRequest
                .builder()
                .tableName(bookingsTableName)
                .key(key)
                .build();
        dynamoDbClient.deleteItem(deleteItemRequest);
    }

    @Override
    public boolean isActiveBooking(Booking booking) {
        LocalTime startTime = LocalTime.now(ZoneId.of("-05:00"));
        return booking.getStartTime().isBefore(startTime) && startTime.isBefore(booking.getEndTime());
    }

}
