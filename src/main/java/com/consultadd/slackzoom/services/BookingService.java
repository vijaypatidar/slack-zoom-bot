package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Booking;
import java.util.List;

public interface BookingService {

    List<Booking> getAllBookings(AccountType accountType);

    void bookAccount(Booking booking);

    void deleteBooking(String bookingId);

    boolean isActiveBooking(Booking booking);
}
