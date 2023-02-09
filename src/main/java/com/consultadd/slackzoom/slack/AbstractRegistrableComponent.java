package com.consultadd.slackzoom.slack;

import com.consultadd.slackzoom.services.AccountService;
import com.consultadd.slackzoom.services.BookingService;
import com.consultadd.slackzoom.slack.view.SlackViews;
import com.slack.api.bolt.App;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public abstract class AbstractRegistrableComponent implements RegistrableComponent, ApplicationEventPublisherAware {
    private AccountService accountService;
    private App app;
    private BookingService bookingService;
    private ApplicationEventPublisher applicationEventPublisher;
    private SlackViews views;

    public App getApp() {
        return app;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    @Autowired
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public BookingService getBookingService() {
        return bookingService;
    }

    @Autowired
    public void setBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public ApplicationEventPublisher getApplicationEventPublisher() {
        return applicationEventPublisher;
    }

    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public SlackViews getViews() {
        return views;
    }

    @Autowired
    public void setViews(SlackViews views) {
        this.views = views;
    }

    @Override
    public void init(App app) {
        this.app = app;
    }
}
