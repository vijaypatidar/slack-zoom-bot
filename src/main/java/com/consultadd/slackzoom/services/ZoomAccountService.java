package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.slack.api.model.view.ViewState;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ZoomAccountService {
    List<ZoomAccount> getAllAccounts();

    ZoomAccount getAccount(String accountId);

    List<ZoomAccount> findAvailableAccounts(LocalTime startTime, LocalTime endTime);

    Map<String, Booking> findActiveBookings();

    Map<String, List<Booking>> findBookings();

    void bookAccount(Booking booking);

    void deleteBooking(String bookingId);

    Optional<Booking> bookAvailableAccount(Map<String, ViewState.Value> state, String userId) throws JsonProcessingException;
}
