package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface ZoomAccountService {
    List<ZoomAccount> getAllAccounts();

    ZoomAccount getAccount(String accountId);
    List<ZoomAccount> findAvailableAccounts(LocalTime startTime, LocalTime endTime);

    Map<String, Booking> findActiveBookings();

    Map<String, Booking> findActiveBookings();

    void bookAccount(Booking booking);

    void deleteBooking(String bookingId);
}
