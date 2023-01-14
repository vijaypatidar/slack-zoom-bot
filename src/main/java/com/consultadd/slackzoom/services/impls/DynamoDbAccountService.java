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

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbAccountService extends AbstractDynamoDbService<Account> implements AccountService {
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNT_TYPE = "accountType";
    public static final String ACCOUNT_NAME = "accountName";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String OWNER_ID = "ownerId";
    private final BookingService bookingService;
    @Value(value = "${DB_ACCOUNTS_TABLE_NAME}")
    String accountsTableName;

    @Override
    protected String getTableName() {
        return this.accountsTableName;
    }

    @Override
    protected Map<String, AttributeValue> toItem(Account account) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ACCOUNT_ID, AttributeValue.builder().s(account.getAccountId()).build());
        item.put(ACCOUNT_TYPE, AttributeValue.builder().s(account.getAccountType().getType()).build());
        item.put(ACCOUNT_NAME, AttributeValue.builder().s(account.getAccountName()).build());
        item.put(USERNAME, AttributeValue.builder().s(account.getUsername()).build());
        item.put(PASSWORD, AttributeValue.builder().s(account.getPassword()).build());
        item.put(OWNER_ID, AttributeValue.builder().s(account.getOwnerId()).build());
        return item;
    }

    @Override
    protected Account toModal(Map<String, AttributeValue> item) {
        return Account
                .builder()
                .accountId(item.get(ACCOUNT_ID).s())
                .username(item.get(USERNAME).s())
                .password(item.get(PASSWORD).s())
                .accountName(item.get(ACCOUNT_NAME).s())
                .accountType(AccountType.valueOf(item.get(ACCOUNT_TYPE).s()))
                .ownerId(item.get(OWNER_ID).s())
                .build();
    }

    @Override
    public List<Account> findAccounts(AccountType accountType) {
        try {
            String matchAccountType = "accountType = :accountType";
            Map<String, AttributeValue> filters = new HashMap<>();
            filters.put(":accountType", AttributeValue.builder().s(accountType.getType()).build());
            return scan(matchAccountType, filters);
        } catch (Exception e) {
            log.error("Error scanning accounts table:{}", accountsTableName, e);
            return List.of();
        }
    }

    @Override
    public Account getAccountById(String accountId) {
        Map<String, AttributeValue> key =
                Map.of(ACCOUNT_ID, AttributeValue.builder().s(accountId).build());
        return getItem(key).orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    public List<Account> getAccountsByOwnerId(String ownerId) {
        try {
            String matchAccountType = "ownerId = :ownerId";
            Map<String, AttributeValue> filters = new HashMap<>();
            filters.put(":ownerId", AttributeValue.builder().s(ownerId).build());
            return scan(matchAccountType, filters);
        } catch (Exception e) {
            log.error("Error scanning accounts table by owner id:{}", accountsTableName, e);
            return List.of();
        }
    }

    private Map<String, List<Booking>> getAccountIdToBookingsMap(AccountType accountType, LocalDate bookingDate) {
        return bookingService
                .findBookings(accountType, bookingDate)
                .stream()
                .collect(Collectors.groupingBy(Booking::getAccountId));
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
                if (isOverlappingTime(startTime, endTime, booking.getStartTime(), booking.getEndTime())) {
                    return false;
                }
            }
            return true;
        }).toList();
    }

    private boolean isOverlappingTime(LocalTime startTime1, LocalTime endTime1, LocalTime startTime2, LocalTime endTime2) {
        return startTime2.isBefore(startTime1) && startTime1.isBefore(endTime2)
                || startTime2.isBefore(endTime1) && endTime1.isBefore(endTime2)
                || startTime1.isBefore(startTime2) && startTime2.isBefore(endTime1)
                || startTime1.isBefore(startTime2) && endTime2.isBefore(endTime1)
                || startTime2.equals(startTime1) || endTime2.equals(endTime1);
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
        putItem(account);
    }

}
