package com.consultadd.slackzoom.slack.actions;

import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import java.io.IOException;
import org.springframework.stereotype.Component;
import static com.consultadd.slackzoom.slack.view.SlackViews.ACTION_RELEASE_BOOKED_ACCOUNT;

@Component
public class FreeAccountRequestAction extends AbstractBlockActionHandler {
    @Override
    public Response apply(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        String bookingId = req.getPayload()
                .getActions()
                .stream()
                .filter(action -> action.getActionId().equals(ACTION_RELEASE_BOOKED_ACCOUNT))
                .findAny()
                .orElseThrow()
                .getValue();
        getBookingService().delete(bookingId);
        getApplicationEventPublisher().publishEvent(new AccountStatusChangeEvent(this));
        getApp().getClient()
                .chatPostMessage(builder -> builder
                        .text("Thank you! Account status has been changed to available.")
                        .channel(req.getPayload().getUser().getId())
                        .token(ctx.getBotToken()));
        return ctx.ack();
    }

    @Override
    String getActionNameRegex() {
        return ACTION_RELEASE_BOOKED_ACCOUNT;
    }

}
