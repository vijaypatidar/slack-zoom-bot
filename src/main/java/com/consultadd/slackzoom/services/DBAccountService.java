package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.utils.TimeUtils;
import com.slack.api.model.view.ViewState;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class DBAccountService implements AccountService {
    private final DynamoDbClient dynamoDbClient;
    private static final String DYNAMODB_ACCOUNTS_TABLE = "slack-bot-zoom-accounts";
    List<Booking> bookings = new LinkedList<>();

    public static Booking mapToBooking(Map<String, AttributeValue> valueMap) {
        return Booking.builder()
                .bookingId(valueMap.get("booking_id").s())
                .accountId(valueMap.get("account_id").s())
                .userId(valueMap.get("user_id").s())
                .startTime(TimeUtils.stringToLocalTime(valueMap.get("start_time").s()))
                .endTime(TimeUtils.stringToLocalTime(valueMap.get("end_time").s()))
                .build();
    }

    @Override
    public List<Account> getAllAccounts(AccountType accountType) {
        try {
            String matchAccountType = "accountType = :accountType";
            Map<String, AttributeValue> filters = new HashMap<>();
            filters.put(":accountType", AttributeValue.builder().s(accountType.getType()).build());
            ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                    .filterExpression(matchAccountType)
                    .expressionAttributeValues(filters)
                    .tableName(DYNAMODB_ACCOUNTS_TABLE).build());
            return scanResponse
                    .items()
                    .stream()
                    .map(Account::toZoomAccount)
                    .toList();
        } catch (Exception e) {
            log.error("Error scanning accounts table", e);
            return List.of();
        }
    }

    @Override
    public Account getAccount(String accountId, AccountType accountType) {
        return getAllAccounts(accountType).stream().filter(za -> za.getAccountId().equals(accountId)).findAny().orElseThrow();
    }

    @Override
    public List<Account> findAvailableAccounts(LocalTime startTime, LocalTime endTime, AccountType accountType) {
        Map<String, List<Booking>> accountIdToBookingsMap = getAccountIdToBookingsMap();
        return getAllAccounts(accountType).stream().filter(account -> {
            for (Booking booking : accountIdToBookingsMap.getOrDefault(account.getAccountId(), new LinkedList<>())) {
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
    public Map<String, Booking> findActiveBookings(AccountType accountType) {
        LocalTime startTime = LocalTime.now(ZoneId.of("-05:00"));
        Map<String, Booking> result = new HashMap<>();
        Map<String, List<Booking>> accountIdToBookingsMap = getAccountIdToBookingsMap();
        getAllAccounts(accountType).forEach(account -> {
            for (Booking booking : accountIdToBookingsMap.getOrDefault(account.getAccountId(), new LinkedList<>())) {
                if (booking.getStartTime().isBefore(startTime) && startTime.isBefore(booking.getEndTime())) {
                    result.put(account.getAccountId(), booking);
                }
            }
        });
        return result;
    }

    @Override
    public Map<String, List<Booking>> findBookings(AccountType accountType) {
        return bookings.stream().collect(Collectors.groupingBy(Booking::getAccountId));
    }

    @Override
    public void bookAccount(Booking booking) {
        bookings.add(booking);
    }

    @Override
    public void deleteBooking(String bookingId) {
        bookings.removeIf(booking -> booking.getBookingId().equals(bookingId));
    }

    public Optional<Booking> bookAvailableAccount(Map<String, ViewState.Value> state, String userId, AccountType accountType) {
        log.info("ViewState:" + state);
        LocalTime startTime = LocalTime.parse(state.get("startTime").getSelectedTime());
        LocalTime endTime = LocalTime.parse(state.get("endTime").getSelectedTime());
        List<Account> availableAccounts = findAvailableAccounts(startTime, endTime, accountType);
        if (availableAccounts.isEmpty()) {
            return Optional.empty();
        } else {
            Account account = availableAccounts.get(0);
            Booking booking = new Booking(startTime, endTime, userId, UUID.randomUUID().toString(), account.getAccountId());
            bookAccount(booking);
            return Optional.of(booking);
        }
    }

    private Map<String, List<Booking>> getAccountIdToBookingsMap() {
        return bookings.stream().collect(Collectors.groupingBy(Booking::getAccountId));
    }

}
