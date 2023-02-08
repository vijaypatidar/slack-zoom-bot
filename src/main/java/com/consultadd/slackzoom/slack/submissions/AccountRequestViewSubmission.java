package com.consultadd.slackzoom.slack.submissions;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.BookingRequest;
import com.consultadd.slackzoom.utils.DateTimeUtils;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.ViewState;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static com.consultadd.slackzoom.services.impls.DynamoDbBookingService.*;
import static com.consultadd.slackzoom.slack.view.SlackViews.SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK;

@RequiredArgsConstructor
@Slf4j
@Component
public class AccountRequestViewSubmission extends AbstractViewSubmissionHandler {

    @Override
    public Response apply(ViewSubmissionRequest req, ViewSubmissionContext ctx) throws IOException, SlackApiException {
        Map<String, ViewState.Value> state = new HashMap<>();
        req.getPayload().getView().getState().getValues().values().forEach(state::putAll);
        LocalTime startTime = DateTimeUtils.stringToLocalTime(state.get(START_TIME).getSelectedTime());
        LocalTime endTime = DateTimeUtils.stringToLocalTime(state.get(END_TIME).getSelectedTime());
        LocalDate bookingDate = DateTimeUtils.stringToDate(state.get(BOOKING_DATE).getSelectedDate());
        String accountType = req.getPayload().getView().getCallbackId().split("-")[1];
        BookingRequest bookingRequest = BookingRequest.builder()
                .userId(req.getPayload().getUser().getId())
                .accountType(AccountType.valueOf(accountType))
                .startTime(startTime)
                .endTime(endTime)
                .bookingDate(bookingDate)
                .build();

        Optional<Booking> optionalBooking = getAccountService().bookAvailableAccount(bookingRequest);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            this.getApplicationEventPublisher().publishEvent(new AccountStatusChangeEvent(this));
            List<LayoutBlock> blocks = getViews().getAccountBookedResponseMessageBlocks(booking);
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

    @Override
    String getCallbackIdRegex() {
        return SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK + ".*";
    }
}
