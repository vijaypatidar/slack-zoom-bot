package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.BookingRequest;
import com.consultadd.slackzoom.models.GetAvailableAccountRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<Account> findAccounts(AccountType accountType);

    Account getAccountById(String accountId);

    List<Account> findAvailableAccounts(GetAvailableAccountRequest request);

    Optional<Booking> bookAvailableAccount(BookingRequest bookingRequest) throws JsonProcessingException;

    void save(Account account);
}
