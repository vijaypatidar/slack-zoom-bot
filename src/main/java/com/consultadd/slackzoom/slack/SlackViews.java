package com.consultadd.slackzoom.slack;

import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.services.ZoomAccountService;
import com.consultadd.slackzoom.utils.TimeUtils;
import com.slack.api.model.block.*;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.MultiStaticSelectElement;
import com.slack.api.model.block.element.TimePickerElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SlackViews {
    public static final String MODAL_VIEW = "modal";
    public static final String PLAIN_TEXT = "plain_text";
    public static final String SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK = "find-zoom-account";
    public static final String DANGER = "danger";
    public static final String ACTION_RELEASE_BOOKED_ACCOUNT = "release_booked_account";
    public static final String ACTION_BOOK_ACCOUNT_REQUEST = "ACTION_BOOK_ACCOUNT_REQUEST";

    private final ZoomAccountService accountService;

    public View getZoomRequestModal() {
        ViewTitle title = ViewTitle.builder()
                .type(PLAIN_TEXT)
                .text("Zoom pro account")
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
                        .text("Please select the duration for which you need pro zoom account.")
                        .build())
                .build());

        blocks.add(InputBlock.builder()
                .element(TimePickerElement
                        .builder()
                        .actionId("startTime")
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
                        .actionId("endTime")
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select end time")
                                .build())
                        .build())
                .label(PlainTextObject
                        .builder()
                        .text("End time")
                        .build())
                .build());

        List<OptionObject> options = accountService.getAllAccounts()
                .stream()
                .map(zoomAccount -> OptionObject
                        .builder()
                        .value(zoomAccount.getAccountId())
                        .text(PlainTextObject
                                .builder()
                                .text(zoomAccount.getAccountName())
                                .build())
                        .build())
                .toList();

        blocks.add(InputBlock.builder()
                .optional(true)
                .element(MultiStaticSelectElement
                        .builder()
                        .actionId("preferred_accounts")
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select zoom account")
                                .build())
                        .options(options)
                        .build())
                .label(PlainTextObject
                        .builder()
                        .text("Preferred zoom account")
                        .build())
                .build());

        return View.builder()
                .type(MODAL_VIEW)
                .callbackId(SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK)
                .title(title)
                .submit(submit)
                .close(close)
                .blocks(blocks)
                .build();
    }

    public List<LayoutBlock> getAccountStatus() {
        List<LayoutBlock> blocks = new LinkedList<>();
        blocks.add(SectionBlock
                .builder()
                .text(PlainTextObject
                        .builder()
                        .text("Zoom accounts status")
                        .build())
                .build());

        blocks.add(DividerBlock.builder().build());

        Map<String, Booking> activeBookings = accountService.findActiveBookings();
        Map<String, List<Booking>> accountIdToBookingMap = accountService.findBookings();

        AtomicInteger count = new AtomicInteger(1);
        accountService.getAllAccounts().forEach(zoomAccount -> {
            Booking booking = activeBookings.get(zoomAccount.getAccountId());
            blocks.add(SectionBlock
                    .builder()
                    .text(MarkdownTextObject
                            .builder()
                            .text("`" + count.getAndIncrement() + ".` *" + zoomAccount.getAccountName() + "*" + (booking == null ? " (available)" : " (in use)"))
                            .build())
                    .build());
            if (booking != null) {
                blocks.add(ContextBlock
                        .builder()
                        .elements(List.of(MarkdownTextObject
                                .builder()
                                .text("<@" +
                                        booking.getUserId() +
                                        "> is using this account right now and it will get available after " +
                                        TimeUtils.timeToString(booking.getEndTime()) +
                                        " EST.")
                                .build()))
                        .build());
            }

            Optional
                    .ofNullable(accountIdToBookingMap.get(zoomAccount.getAccountId()))
                    .ifPresent(bookings -> {
                        String uses = bookings.stream()
                                .map(b -> TimeUtils.timeToString(b.getStartTime()) + " - " + TimeUtils.timeToString(b.getEndTime()) + "\t<@" + b.getUserId() + ">")
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
                        .style("primary")
                        .text(PlainTextObject
                                .builder()
                                .text("Need account")
                                .build())
                        .build()))
                .build());
        return blocks;
    }
}
