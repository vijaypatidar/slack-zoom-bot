package com.consultadd.slackzoom.slack.view.actions;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.slack.view.SlackViews;
import com.consultadd.slackzoom.utils.DateTimeUtils;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.DatePickerElement;
import com.slack.api.model.block.element.TimePickerElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import org.springframework.stereotype.Component;
import static com.consultadd.slackzoom.services.impls.DynamoDbBookingService.*;

@Component
public class BookAccountRequestAction extends AbstractBlockActionHandler {
    @Override
    public Response apply(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        String accountType = req.getPayload()
                .getActions()
                .stream()
                .filter(action -> action.getActionId().equals(SlackViews.ACTION_BOOK_ACCOUNT_REQUEST))
                .findAny()
                .orElseThrow()
                .getValue();
        getApp().getClient()
                .viewsOpen(ViewsOpenRequest
                        .builder()
                        .triggerId(req.getPayload().getTriggerId())
                        .token(ctx.getBotToken())
                        .view(getBookingRequestView(AccountType.valueOf(accountType)))
                        .build()
                );
        return ctx.ack();
    }

    private View getBookingRequestView(AccountType accountType) {
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

    @Override
    public String getActionName() {
        return ACTION_BOOK_ACCOUNT_REQUEST;
    }
}
