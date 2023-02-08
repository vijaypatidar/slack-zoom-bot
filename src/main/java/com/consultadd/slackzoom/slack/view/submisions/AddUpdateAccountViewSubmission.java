package com.consultadd.slackzoom.slack.view.submisions;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.slack.view.SlackViews;
import com.slack.api.app_backend.views.response.ViewSubmissionResponse;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static com.consultadd.slackzoom.services.impls.DynamoDbAccountService.*;
import static com.consultadd.slackzoom.slack.view.SlackViews.ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK;

@Slf4j
@Component
public class AddUpdateAccountViewSubmission extends AbstractViewSubmissionHandler {

    @Override
    public Response apply(ViewSubmissionRequest req, ViewSubmissionContext ctx) throws IOException, SlackApiException {
        Map<String, ViewState.Value> state = new HashMap<>();
        req.getPayload().getView().getState().getValues().values().forEach(state::putAll);
        String callbackId = req.getPayload().getView().getCallbackId();
        log.error("callbackId:{}", callbackId);
        String accountId;
        if (!ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK.equals(callbackId)) {
            accountId = callbackId.replace(ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK, "");
        } else {
            accountId = UUID.randomUUID().toString();
        }
        String accountName = state.get(ACCOUNT_NAME).getValue();
        AccountType accountType = AccountType.valueOf(state.get(ACCOUNT_TYPE).getSelectedOption().getValue());
        String accountUsername = state.get(USERNAME).getValue();
        String accountPassword = state.get(PASSWORD).getValue();
        Account newAccount = Account.builder()
                .accountType(accountType)
                .accountName(accountName)
                .accountId(accountId)
                .ownerId(req.getPayload().getUser().getId())
                .username(accountUsername)
                .password(accountPassword)
                .build();
        getAccountService().save(newAccount);
        getApplicationEventPublisher().publishEvent(new AccountStatusChangeEvent(this));

        View accountDashboard = getViews().getAccountDashboard(req.getPayload().getUser().getId());

        return ctx.ack(ViewSubmissionResponse.builder()
                .view(accountDashboard)
                .responseAction("update").build());
    }

    @Override
    String getCallbackId() {
        return ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK;
    }
}
