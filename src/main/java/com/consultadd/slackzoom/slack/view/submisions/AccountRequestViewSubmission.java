package com.consultadd.slackzoom.slack.view.submisions;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.BookingRequest;
import com.consultadd.slackzoom.slack.view.SlackViews;
import com.consultadd.slackzoom.utils.DateTimeUtils;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.view.ViewState;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import static com.consultadd.slackzoom.services.impls.DynamoDbBookingService.*;

@RequiredArgsConstructor
public class AccountRequestViewSubmission extends AbstractViewSubmissionHandler {

    private final AccountType accountType;

    @Override
    public Response apply(ViewSubmissionRequest req, ViewSubmissionContext ctx) throws IOException, SlackApiException {
        Map<String, ViewState.Value> state = new HashMap<>();
        req.getPayload().getView().getState().getValues().values().forEach(state::putAll);
        LocalTime startTime = DateTimeUtils.stringToLocalTime(state.get(START_TIME).getSelectedTime());
        LocalTime endTime = DateTimeUtils.stringToLocalTime(state.get(END_TIME).getSelectedTime());
        LocalDate bookingDate = DateTimeUtils.stringToDate(state.get(BOOKING_DATE).getSelectedDate());
        BookingRequest bookingRequest = BookingRequest.builder()
                .userId(req.getPayload().getUser().getId())
                .accountType(accountType)
                .startTime(startTime)
                .endTime(endTime)
                .bookingDate(bookingDate)
                .build();

        Optional<Booking> optionalBooking = getAccountService().bookAvailableAccount(bookingRequest);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            this.getApplicationEventPublisher().publishEvent(new AccountStatusChangeEvent(this));
            List<LayoutBlock> blocks = getAccountBookedResponseMessageBlocks(booking);
            getApp().getClient()
                    .chatPostMessage(msg ->
                            msg.channel(req.getPayload().getUser().getId())
                                    .token(ctx.getBotToken())
                                    .blocks(blocks)
                    );
        } else {
            return ctx.ackWithErrors(Map.of("endTimeBlock", "There is no account available for this time frame."));
        }
        return ctx.ack();
    }

    @NotNull
    public List<LayoutBlock> getAccountBookedResponseMessageBlocks(Booking booking) {
        Account account = getAccountService().getAccountById(booking.getAccountId());
        String text = String.format(
                "You can use this account from %s to %s EST on %s.%n```%s%nUsername: %s%nPassword: %s```%n Please update the account state to available, if it get free before the expected end time or if not need anymore.",
                DateTimeUtils.timeToString(booking.getStartTime()),
                DateTimeUtils.timeToString(booking.getEndTime()),
                DateTimeUtils.dateToString(booking.getBookingDate()),
                account.getAccountName(),
                account.getUsername(),
                account.getPassword()
        );
        return List.of(
                SectionBlock.builder()
                        .text(MarkdownTextObject
                                .builder()
                                .text(text)
                                .build())
                        .accessory(ButtonElement
                                .builder()
                                .style(SlackViews.DANGER)
                                .text(PlainTextObject
                                        .builder()
                                        .text("Free Account")
                                        .build())
                                .actionId(SlackViews.ACTION_RELEASE_BOOKED_ACCOUNT)
                                .value(booking.getBookingId())
                                .build())
                        .build());
    }

    @Override
    String getCallbackId() {
        return SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK + ":" + accountType.getType();
    }

    @Override
    public void register(App app) {
        app.viewSubmission(Pattern.compile(getCallbackId() + ".*"), this);
    }
}
