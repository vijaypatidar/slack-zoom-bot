package com.consultadd.slackzoom.slack.view.actions;

import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.slack.view.SlackViews;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.views.ViewsUpdateResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AddUpdateAccountDetailAction extends AbstractBlockActionHandler {
    @Override
    public Response apply(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        Account account = req.getPayload().getActions()
                .stream()
                .filter(action -> SlackViews.ACTION_EDIT_ACCOUNT_DETAIL.equals(action.getActionId()))
                .findAny()
                .map(BlockActionPayload.Action::getValue)
                .map(getAccountService()::getAccountById)
                .orElse(null);
        ViewsUpdateResponse viewsUpdateResponse = getApp().getClient()
                .viewsUpdate(builder -> builder
                        .token(ctx.getBotToken())
                        .view(getViews().addUpdateAccountDetailView(account))
                        .viewId(req.getPayload().getView().getId()));
        if (!viewsUpdateResponse.isOk()) {
            log.error("viewsUpdateResponse:{}", viewsUpdateResponse);
        }
        return ctx.ack();
    }

    @Override
    public String getActionName() {
        return ACTION_EDIT_ACCOUNT_DETAIL;
    }

}
