package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.slack.api.model.view.ViewState;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AccountService {
    List<Account> getAllAccounts(AccountType accountType);

    Account getAccount(String accountId, AccountType accountType);

    List<Account> findAvailableAccounts(LocalTime startTime, LocalTime endTime, AccountType accountType);

    Map<String, List<Booking>> findBookings(AccountType accountType);

    Optional<Booking> bookAvailableAccount(Map<String, ViewState.Value> state, String userId, AccountType accountType) throws JsonProcessingException;
}
