package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import java.util.List;
import java.util.Map;

public interface ZoomAccountService {
    List<ZoomAccount> getAllAccounts();

    List<ZoomAccount> findAvailableAccounts(int startTime, int endTime);

    Map<String, Booking> findActiveBookings();

    void bookAccount(Booking booking);
}
