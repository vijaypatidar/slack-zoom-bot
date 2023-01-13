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

    public static final String BOOKING_ID = "bookingId";
    public static final String ACCOUNT_ID = "accountId";
    public static final String USER_ID = "userId";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String BOOKING_DATE = "bookingDate";
    @Value(value = "${DB_BOOKINGS_TABLE_NAME}")
    String bookingsTableName;

    public Booking mapToBooking(Map<String, AttributeValue> valueMap) {
        return Booking.builder()
                .bookingId(valueMap.get(BOOKING_ID).s())
                .accountId(valueMap.get(ACCOUNT_ID).s())
                .userId(valueMap.get(USER_ID).s())
                .startTime(DateTimeUtils.stringToLocalTime(valueMap.get(START_TIME).s()))
                .endTime(DateTimeUtils.stringToLocalTime(valueMap.get(END_TIME).s()))
                .bookingDate(DateTimeUtils.stringToDate(valueMap.get(BOOKING_DATE).s()))
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
            log.error("Error scanning bookings table", e);
            return List.of();
        }
    }

    @Override
    public void save(Booking booking) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(BOOKING_ID, AttributeValue.builder().s(booking.getBookingId()).build());
        item.put(ACCOUNT_ID, AttributeValue.builder().s(booking.getAccountId()).build());
        item.put(USER_ID, AttributeValue.builder().s(booking.getUserId()).build());
        item.put(START_TIME, AttributeValue.builder().s(DateTimeUtils.timeToString(booking.getStartTime())).build());
        item.put(END_TIME, AttributeValue.builder().s(DateTimeUtils.timeToString(booking.getEndTime())).build());
        item.put(BOOKING_DATE, AttributeValue.builder().s(DateTimeUtils.dateToString(booking.getBookingDate())).build());
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .item(item)
                .tableName(bookingsTableName)
                .build();
        getDynamoDbClient().putItem(putItemRequest);
    }

    @Override
    public void delete(String bookingId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(BOOKING_ID, AttributeValue.builder().s(bookingId).build());
        DeleteItemRequest deleteItemRequest = DeleteItemRequest
                .builder()
                .tableName(bookingsTableName)
                .key(key)
                .build();
        getDynamoDbClient().deleteItem(deleteItemRequest);
    }

    @Override
    public boolean isActiveBooking(Booking booking) {
        LocalTime startTime = LocalTime.now(DateTimeUtils.ZONE_ID);
        return booking.getStartTime().isBefore(startTime) && startTime.isBefore(booking.getEndTime());
    }

}
