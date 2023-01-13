package com.consultadd.slackzoom.slack;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.services.AccountService;
import com.consultadd.slackzoom.services.BookingService;
import com.consultadd.slackzoom.utils.DateTimeUtils;
import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.*;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static com.consultadd.slackzoom.services.impls.DynamoDbAccountService.*;
import static com.consultadd.slackzoom.services.impls.DynamoDbBookingService.*;
import static com.consultadd.slackzoom.utils.DateTimeUtils.ZONE_ID;

@Component
@Slf4j
@RequiredArgsConstructor
public class SlackViews {
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

    private final AccountService accountService;
    private final BookingService bookingService;

    public View getBookingRequestView(AccountType accountType) {
        ViewTitle title = ViewTitle.builder()
                .type(PLAIN_TEXT)
                .text(accountType.getDisplayName())
                .build();
        ViewSubmit submit = ViewSubmit.builder()
                .type(PLAIN_TEXT)
                .text("Submit")
                .build();
        ViewClose close = ViewClose.builder()
                .type(PLAIN_TEXT)
                .text("Cancel")
                .build();

        List<LayoutBlock> blocks = new LinkedList<>();

        blocks.add(SectionBlock.builder()
                .text(PlainTextObject.builder()
                        .text("Please select the day and duration for which you need " + accountType.getDisplayName() + ".")
                        .build())
                .build());

        blocks.add(InputBlock.builder()
                .element(DatePickerElement.builder()
                        .actionId(BOOKING_DATE)
                        .initialDate(DateTimeUtils.dateToString(LocalDate.now()))
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select booking date")
                                .build())
                        .build())
                .label(PlainTextObject.builder()
                        .text("Booking date")
                        .build())
                .build());

        blocks.add(InputBlock.builder()
                .element(TimePickerElement
                        .builder()
                        .actionId(START_TIME)
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select start time")
                                .build())
                        .build())
                .label(PlainTextObject
                        .builder()
                        .text("Start time")
                        .build())
                .build());

        blocks.add(InputBlock.builder()
                .element(TimePickerElement
                        .builder()
                        .actionId(END_TIME)
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select end time")
                                .build())
                        .build())
                .label(PlainTextObject
                        .builder()
                        .text("End time")
                        .build())
                .blockId("endTimeBlock")
                .build());

        return View.builder()
                .type(MODAL_VIEW)
                .callbackId(SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK + ":" + accountType.getType())
                .title(title)
                .submit(submit)
                .close(close)
                .blocks(blocks)
                .build();
    }

    public View addUpdateAccountDetailView(Account account) {
        ViewTitle title = ViewTitle.builder()
                .type(PLAIN_TEXT)
                .text(account == null ? "Add new account" : "Update account detail")
                .build();
        ViewSubmit submit = ViewSubmit.builder()
                .type(PLAIN_TEXT)
                .text(account == null ? "Add Account" : "Update account")
                .build();
        ViewClose close = ViewClose.builder()
                .type(PLAIN_TEXT)
                .text("Cancel")
                .build();

        List<LayoutBlock> blocks = new LinkedList<>();
        blocks.add(InputBlock.builder()
                .label(PlainTextObject.builder()
                        .text("Account name")
                        .build())
                .element(PlainTextInputElement
                        .builder()
                        .actionId(ACCOUNT_NAME)
                        .initialValue(Optional.ofNullable(account).map(Account::getAccountName).orElse(null))
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Enter account name")
                                .build())
                        .build())
                .build());

        List<OptionObject> options = Stream.of(AccountType.values())
                .map(accountType -> OptionObject
                        .builder()
                        .text(PlainTextObject
                                .builder()
                                .text(accountType.getDisplayName())
                                .build())
                        .value(accountType.getType())
                        .build()).toList();

        OptionObject selectedOption = Optional.ofNullable(account).flatMap(acc -> options.stream()
                .filter(optionObject -> optionObject.getValue().equals(acc.getAccountType().getType()))
                .findAny()).orElse(null);


        blocks.add(InputBlock.builder()
                .label(PlainTextObject
                        .builder()
                        .text("Account type")
                        .build())
                .element(StaticSelectElement
                        .builder()
                        .actionId(ACCOUNT_TYPE)
                        .initialOption(selectedOption)
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select account type")
                                .build())
                        .options(options)
                        .build())
                .build());

        blocks.add(InputBlock.builder()
                .label(PlainTextObject.builder()
                        .text("Username")
                        .build())
                .element(PlainTextInputElement
                        .builder()
                        .actionId(USERNAME)
                        .initialValue(Optional.ofNullable(account).map(Account::getUsername).orElse(null))
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Enter account username/email")
                                .build())
                        .build())
                .build());
        blocks.add(InputBlock.builder()
                .label(PlainTextObject.builder()
                        .text("Password")
                        .build())
                .element(PlainTextInputElement
                        .builder()
                        .actionId(PASSWORD)
                        .initialValue(Optional.ofNullable(account).map(Account::getPassword).orElse(null))
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Enter account password")
                                .build())
                        .build())
                .build());

        return View.builder()
                .type(MODAL_VIEW)
                .callbackId(ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK +
                        Optional.ofNullable(account).map(Account::getAccountId).orElse(""))
                .title(title)
                .submit(submit)
                .close(close)
                .blocks(blocks)
                .build();
    }

    public List<LayoutBlock> getAccountStatusMessageView() {
        List<LayoutBlock> blocks = new LinkedList<>();
        blocks.add(HeaderBlock
                .builder()
                .text(PlainTextObject
                        .builder()
                        .text("Accounts status")
                        .build())
                .build());
        blocks.addAll(Stream
                .of(AccountType.values())
                .map(this::getAccountStatusMessageView)
                .filter(b -> b.size() > 2)
                .reduce((l1, l2) -> {
                    List<LayoutBlock> merge = new LinkedList<>(l1);
                    merge.add(DividerBlock.builder().build());
                    merge.addAll(l2);
                    return merge;
                }).orElse(List.of()));
        return blocks;
    }

    private List<LayoutBlock> getAccountStatusMessageView(AccountType accountType) {
        List<LayoutBlock> blocks = new LinkedList<>();
        blocks.add(SectionBlock
                .builder()
                .text(MarkdownTextObject
                        .builder()
                        .text("*" + accountType.getDisplayName() + "*")
                        .build())
                .build());

        Map<String, List<Booking>> accountIdToBookings = bookingService
                .findBookings(accountType, LocalDate.now(ZONE_ID))
                .stream()
                .collect(Collectors.groupingBy(Booking::getAccountId));

        AtomicInteger count = new AtomicInteger(1);
        accountService.findAccounts(accountType).forEach(zoomAccount -> {
            Booking activeBooking = Optional
                    .ofNullable(accountIdToBookings.get(zoomAccount.getAccountId()))
                    .orElse(List.of())
                    .stream()
                    .filter(bookingService::isActiveBooking)
                    .findAny().orElse(null);
            blocks.add(SectionBlock
                    .builder()
                    .text(MarkdownTextObject
                            .builder()
                            .text("`" + count.getAndIncrement() + ".` *" + zoomAccount.getAccountName() + "*" + (activeBooking == null ? " (available)" : " (in use)"))
                            .build())
                    .build());
            if (activeBooking != null) {
                blocks.add(ContextBlock
                        .builder()
                        .elements(List.of(MarkdownTextObject
                                .builder()
                                .text("<@" +
                                        activeBooking.getUserId() +
                                        "> is using this account right now and it will get available after " +
                                        DateTimeUtils.timeToString(activeBooking.getEndTime()) +
                                        " EST.")
                                .build()))
                        .build());
            }

            Optional
                    .ofNullable(accountIdToBookings.get(zoomAccount.getAccountId()))
                    .ifPresent(bookings -> {
                        String uses = bookings.stream()
                                .sorted(Comparator.comparing(Booking::getStartTime))
                                .map(b -> DateTimeUtils.timeToString(b.getStartTime()) + " - " + DateTimeUtils.timeToString(b.getEndTime()) + "\t<@" + b.getUserId() + ">")
                                .reduce((a, b) -> a + "\n" + b)
                                .orElse("");

                        blocks.add(ContextBlock
                                .builder()
                                .elements(List.of(MarkdownTextObject
                                        .builder()
                                        .text(uses)
                                        .build()))
                                .build());
                    });


        });

        blocks.add(ActionsBlock
                .builder()
                .elements(List.of(ButtonElement
                        .builder()
                        .actionId(ACTION_BOOK_ACCOUNT_REQUEST)
                        .value(accountType.getType())
                        .style(PRIMARY)
                        .text(PlainTextObject
                                .builder()
                                .text("Need account")
                                .build())
                        .build()))
                .build());
        return blocks;
    }

    public View getAccountDashboard() {
        ViewTitle title = ViewTitle.builder()
                .type(PLAIN_TEXT)
                .text("Accounts Dashboard")
                .build();
        ViewClose close = ViewClose.builder()
                .type(PLAIN_TEXT)
                .text("Close")
                .build();
        List<LayoutBlock> blocks = new LinkedList<>();
        blocks.add(DividerBlock.builder().build());
        blocks.addAll(Stream.of(AccountType.values())
                .map(this::getDashboardItemAccount)
                .reduce((l1, l2) -> {
                    List<LayoutBlock> merge = new LinkedList<>(l1);
                    merge.add(DividerBlock.builder().build());
                    merge.addAll(l2);
                    return merge;
                }).orElse(List.of()));
        return View.builder()
                .type(MODAL_VIEW)
                .callbackId(ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK)
                .title(title)
                .close(close)
                .blocks(blocks)
                .build();
    }

    private List<LayoutBlock> getDashboardItemAccount(AccountType accountType) {
        List<LayoutBlock> blocks = new LinkedList<>();
        blocks.add(SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text("*" + accountType.getDisplayName() + "*").build())
                .accessory(ButtonElement
                        .builder()
                        .text(PlainTextObject
                                .builder()
                                .text("Add new")
                                .build())
                        .actionId(ACTION_EDIT_ACCOUNT_DETAIL)
                        .style(PRIMARY)
                        .build())
                .build());
        AtomicInteger count = new AtomicInteger(1);
        accountService.findAccounts(accountType)
                .forEach(account -> blocks.add(SectionBlock
                        .builder()
                        .text(MarkdownTextObject
                                .builder()
                                .text(String.format("`%s.` %s", count.getAndIncrement(), account.getAccountName()))
                                .build())
                        .accessory(ButtonElement.builder()
                                .text(PlainTextObject.builder()
                                        .emoji(true)
                                        .text("Edit").build())
                                .style(PRIMARY)
                                .value(account.getAccountId())
                                .actionId(ACTION_EDIT_ACCOUNT_DETAIL)
                                .build())
                        .build()));
        return blocks;
    }
}
