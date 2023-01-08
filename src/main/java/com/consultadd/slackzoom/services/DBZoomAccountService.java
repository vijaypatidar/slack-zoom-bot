package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
    Map<String, List<Booking>> bookings = new HashMap<>();

    private String accountsTable = "slack-bot-zoom-accounts";

    private final DynamoDbClient dynamoDbClient;

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
    public List<ZoomAccount> findAvailableAccounts(int startTime, int endTime) {
        return getAllAccounts().stream().filter(zoomAccount -> {
            for (Booking booking : bookings.getOrDefault(zoomAccount.getAccountId(), new LinkedList<>())) {
                if (
                        booking.getStartTime() <= startTime && startTime < booking.getEndTime()
                                || booking.getStartTime() <= endTime && endTime <= booking.getEndTime()) {
                    return false;
                }
            }
            return true;
        }).toList();
    }

    @Override
    public void bookAccount(Booking booking) {
        List<Booking> bookingList = bookings.getOrDefault(booking.getAccountId(), new LinkedList<>());
        bookingList.add(booking);
        bookings.put(booking.getAccountId(), bookingList);
    }


}
