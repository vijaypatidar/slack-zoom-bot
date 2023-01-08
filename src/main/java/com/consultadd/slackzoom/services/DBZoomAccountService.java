package com.consultadd.slackzoom.services;

import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DBZoomAccountService implements ZoomAccountService {
    List<ZoomAccount> zoomAccounts = new LinkedList<>();
    Map<String, List<Booking>> bookings = new HashMap<>();

    public DBZoomAccountService() {
        zoomAccounts.add(ZoomAccount.builder()
                .accountName("Vijay's zoom account")
                .username("user1")
                .password("123456780")
                .accountId("ac-1").build());
        zoomAccounts.add(ZoomAccount.builder()
                .accountName("Pragati's zoom account")
                .username("user2")
                .password("12345678")
                .accountId("ac-2").build());
        zoomAccounts.add(ZoomAccount.builder()
                .accountName("Suhas's zoom account")
                .username("user3")
                .password("12345678")
                .accountId("ac-3").build());
    }

    @Override
    public List<ZoomAccount> getAllAccounts() {
        return zoomAccounts;
    }

    @Override
    public List<ZoomAccount> findAvailableAccounts(int startTime, int endTime) {
        return zoomAccounts.stream().filter(zoomAccount -> {
            for (Booking booking : bookings.getOrDefault(zoomAccount.getAccountId(), new LinkedList<>())) {
                if (
                        booking.getStartTime() <= startTime && startTime < booking.getEndTime()
                                || booking.getStartTime() <= endTime && endTime <= booking.getEndTime()) {
                    return false;
                }
            }
            return true;
        }).toList();
    }

    @Override
    public void bookAccount(Booking booking) {
        List<Booking> bookingList = bookings.getOrDefault(booking.getAccountId(), new LinkedList<>());
        bookingList.add(booking);
        bookings.put(booking.getAccountId(), bookingList);
    }


}
