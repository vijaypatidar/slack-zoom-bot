package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Booking;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    List<Booking> findBookings(AccountType accountType, LocalDate bookingDate);

    void save(Booking booking);

    void delete(String bookingId);

    boolean isActiveBooking(Booking booking);
}
