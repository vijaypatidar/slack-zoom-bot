package com.consultadd.slackzoom.services.impls;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.services.BookingService;
import com.consultadd.slackzoom.utils.DateTimeUtils;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbBookingService extends AbstractDynamoDbService<Booking> implements BookingService {

    public static final String BOOKING_ID = "bookingId";
    public static final String ACCOUNT_ID = "accountId";
    public static final String USER_ID = "userId";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String BOOKING_DATE = "bookingDate";
    @Value(value = "${DB_BOOKINGS_TABLE_NAME}")
    String bookingsTableName;


    @Override
    public List<Booking> findBookings(AccountType accountType, LocalDate bookingDate) {
        try {
            String matchBookingDate = "bookingDate = :bookingDate";
            Map<String, AttributeValue> filters = new HashMap<>();
            filters.put(":bookingDate", AttributeValue.builder().s(DateTimeUtils.dateToString(bookingDate)).build());
            return scan(matchBookingDate, filters);
        } catch (Exception e) {
            log.error("Error scanning bookings table", e);
            return List.of();
        }
    }

    @Override
    public void save(Booking booking) {
        putItem(booking);
    }

    @Override
    public void delete(String bookingId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(BOOKING_ID, AttributeValue.builder().s(bookingId).build());
        deleteItem(key);
    }

    @Override
    public boolean isActiveBooking(Booking booking) {
        LocalTime startTime = LocalTime.now(DateTimeUtils.ZONE_ID);
        return booking.getStartTime().isBefore(startTime) && startTime.isBefore(booking.getEndTime());
    }

    @Override
    protected Booking toModal(Map<String, AttributeValue> item) {
        return Booking.builder()
                .bookingId(item.get(BOOKING_ID).s())
                .accountId(item.get(ACCOUNT_ID).s())
                .userId(item.get(USER_ID).s())
                .startTime(DateTimeUtils.stringToLocalTime(item.get(START_TIME).s()))
                .endTime(DateTimeUtils.stringToLocalTime(item.get(END_TIME).s()))
                .bookingDate(DateTimeUtils.stringToDate(item.get(BOOKING_DATE).s()))
                .build();
    }

    @Override
    protected Map<String, AttributeValue> toItem(Booking booking) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(BOOKING_ID, AttributeValue.builder().s(booking.getBookingId()).build());
        item.put(ACCOUNT_ID, AttributeValue.builder().s(booking.getAccountId()).build());
        item.put(USER_ID, AttributeValue.builder().s(booking.getUserId()).build());
        item.put(START_TIME, AttributeValue.builder().s(DateTimeUtils.timeToString(booking.getStartTime())).build());
        item.put(END_TIME, AttributeValue.builder().s(DateTimeUtils.timeToString(booking.getEndTime())).build());
        item.put(BOOKING_DATE, AttributeValue.builder().s(DateTimeUtils.dateToString(booking.getBookingDate())).build());
        return item;
    }

    @Override
    protected String getTableName() {
        return this.bookingsTableName;
    }
}
