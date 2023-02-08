package com.consultadd.slackzoom.slack.view;

import com.consultadd.slackzoom.services.AccountService;
import com.consultadd.slackzoom.services.BookingService;
import com.slack.api.bolt.App;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

public abstract class AbstractRegistrableComponent implements RegistrableComponent, ApplicationEventPublisherAware {
    public static final String MODAL_VIEW = "modal";
    public static final String PLAIN_TEXT = "plain_text";
    public static final String SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK = "find-zoom-account";
    public static final String ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK = "ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK:";
    public static final String DANGER = "danger";
    public static final String ACTION_RELEASE_BOOKED_ACCOUNT = "ACTION_RELEASE_BOOKED_ACCOUNT";
    public static final String ACTION_BOOK_ACCOUNT_REQUEST = "ACTION_BOOK_ACCOUNT_REQUEST";
    public static final String PRIMARY = "primary";
    public static final String ACTION_EDIT_ACCOUNT_DETAIL = "EDIT_ACCOUNT_DETAIL";
    public static final String DASHBOARD_VIEW_ID = "DASHBOARD_VIEW_ID";

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
