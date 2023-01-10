package com.consultadd.slackzoom.services.impls;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.services.AccountService;
import com.consultadd.slackzoom.services.BookingService;
import com.slack.api.model.view.ViewState;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbAccountService implements AccountService {
    @Value(value = "${DB_ACCOUNTS_TABLE_NAME}")
    String accountsTableName;
    private final DynamoDbClient dynamoDbClient;
    private final BookingService bookingService;

    @Override
    public List<Account> getAllAccounts(AccountType accountType) {
        try {
            String matchAccountType = "accountType = :accountType";
            Map<String, AttributeValue> filters = new HashMap<>();
            filters.put(":accountType", AttributeValue.builder().s(accountType.getType()).build());
            ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                    .filterExpression(matchAccountType)
                    .expressionAttributeValues(filters)
                    .tableName(accountsTableName).build());
            return scanResponse
                    .items()
                    .stream()
                    .map(this::toAccount)
                    .toList();
        } catch (Exception e) {
            log.error("Error scanning bookings table", e);
            return List.of();
        }
    }

    @Override
    public Account getAccount(String accountId, AccountType accountType) {
        return getAllAccounts(accountType).stream().filter(za -> za.getAccountId().equals(accountId)).findAny().orElseThrow();
    }

    @Override
    public List<Account> findAvailableAccounts(LocalTime startTime, LocalTime endTime, AccountType accountType) {
        Map<String, List<Booking>> accountIdToBookingsMap = getAccountIdToBookingsMap(accountType);
        return getAllAccounts(accountType).stream().filter(account -> {
            for (Booking booking : accountIdToBookingsMap.getOrDefault(account.getAccountId(), new LinkedList<>())) {
                if (
                        booking.getStartTime().isBefore(startTime) && startTime.isBefore(booking.getEndTime())
                                || booking.getStartTime().isBefore(endTime) && endTime.isBefore(booking.getEndTime())
                                || startTime.isBefore(booking.getStartTime()) && booking.getStartTime().isBefore(endTime)
                                || startTime.isBefore(booking.getEndTime()) && booking.getEndTime().isBefore(endTime)
                                || booking.getStartTime().equals(startTime) || booking.getEndTime().equals(endTime)
                ) {
                    return false;
                }
            }
            return true;
        }).toList();
    }


    @Override
    public Map<String, List<Booking>> findBookings(AccountType accountType) {
        return bookingService
                .getAllBookings(accountType)
                .stream()
                .collect(Collectors.groupingBy(Booking::getAccountId));
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
            Booking booking = Booking.builder()
                    .bookingId(UUID.randomUUID().toString())
                    .startTime(startTime)
                    .endTime(endTime)
                    .userId(userId)
                    .accountId(account.getAccountId())
                    .build();
            bookingService.bookAccount(booking);
            return Optional.of(booking);
        }
    }

    private Map<String, List<Booking>> getAccountIdToBookingsMap(AccountType accountType) {
        return bookingService
                .getAllBookings(accountType)
                .stream()
                .collect(Collectors.groupingBy(Booking::getAccountId));
    }

    public Account toAccount(Map<String, AttributeValue> valueMap) {
        return Account
                .builder()
                .accountId(valueMap.get("account_id").s())
                .username(valueMap.get("username").s())
                .password(valueMap.get("password").s())
                .accountName(valueMap.get("account_name").s())
                .accountType(AccountType.ZOOM)
                .build();
    }
}
