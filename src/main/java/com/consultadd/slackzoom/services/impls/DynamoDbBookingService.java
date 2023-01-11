package com.consultadd.slackzoom.services.impls;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.services.BookingService;
import com.consultadd.slackzoom.utils.DateTimeUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbBookingService extends AbstractDynamoDbService implements BookingService {

    @Value(value = "${DB_BOOKINGS_TABLE_NAME}")
    String bookingsTableName;

    public Booking mapToBooking(Map<String, AttributeValue> valueMap) {
        return Booking.builder()
                .bookingId(valueMap.get("bookingId").s())
                .accountId(valueMap.get("accountId").s())
                .userId(valueMap.get("userId").s())
                .startTime(DateTimeUtils.stringToLocalTime(valueMap.get("startTime").s()))
                .endTime(DateTimeUtils.stringToLocalTime(valueMap.get("endTime").s()))
                .bookingDate(DateTimeUtils.stringToDate(valueMap.get("bookingDate").s()))
                .build();
    }


    @Override
    public List<Booking> findBookings(AccountType accountType, LocalDate bookingDate) {
        try {
            String matchBookingDate = "bookingDate = :bookingDate";
            Map<String, AttributeValue> filters = new HashMap<>();
            filters.put(":bookingDate", AttributeValue.builder().s(DateTimeUtils.dateToString(bookingDate)).build());
            ScanResponse scanResponse = getDynamoDbClient().scan(ScanRequest.builder()
                    .filterExpression(matchBookingDate)
                    .expressionAttributeValues(filters)
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
    public void save(Booking booking) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("bookingId", AttributeValue.builder().s(booking.getBookingId()).build());
        item.put("accountId", AttributeValue.builder().s(booking.getAccountId()).build());
        item.put("userId", AttributeValue.builder().s(booking.getUserId()).build());
        item.put("startTime", AttributeValue.builder().s(DateTimeUtils.timeToString(booking.getStartTime())).build());
        item.put("endTime", AttributeValue.builder().s(DateTimeUtils.timeToString(booking.getEndTime())).build());
        item.put("bookingDate", AttributeValue.builder().s(DateTimeUtils.dateToString(booking.getBookingDate())).build());
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .item(item)
                .tableName(bookingsTableName)
                .build();
        getDynamoDbClient().putItem(putItemRequest);
    }

    @Override
    public void delete(String bookingId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("bookingId", AttributeValue.builder().s(bookingId).build());
        DeleteItemRequest deleteItemRequest = DeleteItemRequest
                .builder()
                .tableName(bookingsTableName)
                .key(key)
                .build();
        getDynamoDbClient().deleteItem(deleteItemRequest);
    }

    @Override
    public boolean isActiveBooking(Booking booking) {
        LocalTime startTime = LocalTime.now(ZoneId.of(DateTimeUtils.ZONE_ID));
        return booking.getStartTime().isBefore(startTime) && startTime.isBefore(booking.getEndTime());
    }

}
