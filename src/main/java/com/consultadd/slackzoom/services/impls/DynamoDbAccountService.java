package com.consultadd.slackzoom.services.impls;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.BookingRequest;
import com.consultadd.slackzoom.models.GetAvailableAccountRequest;
import com.consultadd.slackzoom.services.AccountService;
import com.consultadd.slackzoom.services.BookingService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbAccountService extends AbstractDynamoDbService implements AccountService {
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNT_TYPE = "accountType";
    public static final String ACCOUNT_NAME = "accountName";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    private final BookingService bookingService;
    @Value(value = "${DB_ACCOUNTS_TABLE_NAME}")
    String accountsTableName;

    @Override
    public List<Account> findAccounts(AccountType accountType) {
        try {
            String matchAccountType = "accountType = :accountType";
            Map<String, AttributeValue> filters = new HashMap<>();
            filters.put(":accountType", AttributeValue.builder().s(accountType.getType()).build());
            ScanResponse scanResponse = getDynamoDbClient().scan(ScanRequest.builder()
                    .filterExpression(matchAccountType)
                    .expressionAttributeValues(filters)
                    .tableName(accountsTableName).build());
            return scanResponse
                    .items()
                    .stream()
                    .map(this::toAccount)
                    .toList();
        } catch (Exception e) {
            log.error("Error scanning bookings table{}", accountsTableName, e);
            return List.of();
        }
    }

    @Override
    public Account getAccount(String accountId, AccountType accountType) {
        return findAccounts(accountType).stream().filter(za -> za.getAccountId().equals(accountId)).findAny().orElseThrow();
    }

    @Override
    public List<Account> findAvailableAccounts(GetAvailableAccountRequest request) {
        AccountType accountType = request.getAccountType();
        LocalDate bookingDate = request.getBookingDate();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();
        Map<String, List<Booking>> accountIdToBookingsMap = getAccountIdToBookingsMap(accountType, bookingDate);
        return findAccounts(accountType).stream().filter(account -> {
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
    public Optional<Booking> bookAvailableAccount(BookingRequest request) {
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();
        LocalDate bookingDate = request.getBookingDate();

        List<Account> availableAccounts = findAvailableAccounts(GetAvailableAccountRequest
                .builder()
                .accountType(request.getAccountType())
                .bookingDate(request.getBookingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build());

        if (availableAccounts.isEmpty()) {
            return Optional.empty();
        } else {
            Account account = availableAccounts.get(0);
            Booking booking = Booking.builder()
                    .bookingId(UUID.randomUUID().toString())
                    .startTime(startTime)
                    .endTime(endTime)
                    .userId(request.getUserId())
                    .bookingDate(bookingDate)
                    .accountId(account.getAccountId())
                    .build();
            bookingService.save(booking);
            return Optional.of(booking);
        }
    }

    @Override
    public void save(Account account) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ACCOUNT_ID, AttributeValue.builder().s(account.getAccountId()).build());
        item.put(ACCOUNT_TYPE, AttributeValue.builder().s(account.getAccountType().getType()).build());
        item.put(ACCOUNT_NAME, AttributeValue.builder().s(account.getAccountName()).build());
        item.put(USERNAME, AttributeValue.builder().s(account.getUsername()).build());
        item.put(PASSWORD, AttributeValue.builder().s(account.getPassword()).build());
        PutItemRequest putItemRequest = PutItemRequest
                .builder()
                .item(item)
                .tableName(accountsTableName)
                .build();
        getDynamoDbClient().putItem(putItemRequest);
    }

    private Map<String, List<Booking>> getAccountIdToBookingsMap(AccountType accountType, LocalDate bookingDate) {
        return bookingService
                .findBookings(accountType, bookingDate)
                .stream()
                .collect(Collectors.groupingBy(Booking::getAccountId));
    }

    public Account toAccount(Map<String, AttributeValue> valueMap) {
        return Account
                .builder()
                .accountId(valueMap.get(ACCOUNT_ID).s())
                .username(valueMap.get(USERNAME).s())
                .password(valueMap.get(PASSWORD).s())
                .accountName(valueMap.get(ACCOUNT_NAME).s())
                .accountType(AccountType.valueOf(valueMap.get(ACCOUNT_TYPE).s()))
                .build();
    }
}
