package com.consultadd.slackzoom.slack.view.actions;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.slack.view.SlackViews;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;
import static com.consultadd.slackzoom.slack.view.SlackViews.ACTION_BOOK_ACCOUNT_REQUEST;

@Component
public class BookAccountRequestAction extends AbstractBlockActionHandler {
    @Override
    public Response apply(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        String accountType = req.getPayload()
                .getActions()
                .stream()
                .filter(action -> action.getActionId().equals(ACTION_BOOK_ACCOUNT_REQUEST))
                .findAny()
                .orElseThrow()
                .getValue();
        getApp().getClient()
                .viewsOpen(ViewsOpenRequest
                        .builder()
                        .triggerId(req.getPayload().getTriggerId())
                        .token(ctx.getBotToken())
                        .view(getViews().getBookingRequestView(AccountType.valueOf(accountType)))
                        .build()
                );
        return ctx.ack();
    }

    @Override
    public String getActionName() {
        return ACTION_BOOK_ACCOUNT_REQUEST;
    }
}
