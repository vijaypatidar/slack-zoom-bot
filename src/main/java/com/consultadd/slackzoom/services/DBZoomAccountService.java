package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class DBZoomAccountService implements ZoomAccountService {
    private final DynamoDbClient dynamoDbClient;
    List<Booking> bookings = new LinkedList<>();
    private final String accountsTable = "slack-bot-zoom-accounts";

    @Override
    public List<ZoomAccount> getAllAccounts() {
        try {
            ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName(accountsTable).build());
            return scanResponse
                    .items()
                    .stream()
                    .map(ZoomAccount::toZoomAccount)
                    .toList();
        } catch (Exception e) {
            log.error("Error scanning accounts table", e);
            return List.of();
        }
    }

    @Override
    public ZoomAccount getAccount(String accountId) {
        return getAllAccounts().stream().filter(za -> za.getAccountId().equals(accountId)).findAny().orElseThrow();
    }

    @Override
    public List<ZoomAccount> findAvailableAccounts(LocalTime startTime, LocalTime endTime) {
        Map<String, List<Booking>> accountIdToBookingsMap = getAccountIdToBookingsMap();
        return getAllAccounts().stream().filter(zoomAccount -> {
            for (Booking booking : accountIdToBookingsMap.getOrDefault(zoomAccount.getAccountId(), new LinkedList<>())) {
                if (
                        booking.getStartTime().isBefore(startTime) && startTime.isBefore(booking.getEndTime())
                                || booking.getStartTime().isBefore(endTime) && endTime.isBefore(booking.getEndTime())
                                || startTime.isBefore(booking.getStartTime()) && booking.getStartTime().isBefore(endTime)
                                || startTime.isBefore(booking.getEndTime()) && booking.getEndTime().isBefore(endTime)
                ) {
                    return false;
                }
            }
            return true;
        }).toList();
    }

    @Override
    public Map<String, Booking> findActiveBookings() {
        LocalTime startTime = LocalTime.now(ZoneId.of("-05:00"));
        Map<String, Booking> result = new HashMap<>();
        Map<String, List<Booking>> accountIdToBookingsMap = getAccountIdToBookingsMap();
        getAllAccounts().forEach(zoomAccount -> {
            for (Booking booking : accountIdToBookingsMap.getOrDefault(zoomAccount.getAccountId(), new LinkedList<>())) {
                if (booking.getStartTime().isBefore(startTime) && startTime.isBefore(booking.getEndTime())) {
                    result.put(zoomAccount.getAccountId(), booking);
                }
            }
        });
        return result;
    }

    @Override
    public void bookAccount(Booking booking) {
        bookings.add(booking);
    }

    @Override
    public void deleteBooking(String bookingId) {
        bookings.removeIf(booking -> booking.getBookingId().equals(bookingId));
    }

    private Map<String, List<Booking>> getAccountIdToBookingsMap() {
        return bookings.stream().collect(Collectors.groupingBy(Booking::getAccountId));
    }


}
