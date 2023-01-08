package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import java.util.List;

public interface ZoomAccountService {
    List<ZoomAccount> getAllAccounts();

    List<ZoomAccount> findAvailableAccounts(int startTime, int endTime);

    void bookAccount(Booking booking);
}
