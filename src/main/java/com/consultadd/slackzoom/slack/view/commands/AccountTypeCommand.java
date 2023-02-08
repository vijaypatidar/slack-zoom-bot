package com.consultadd.slackzoom.slack.view.commands;

import com.consultadd.slackzoom.enums.AccountType;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountTypeCommand extends AbstractSlashCommandHandler {
    private final AccountType accountType;

    @Override
    public String getCommandName() {
        return null;
    }

    @Override
    public Response apply(SlashCommandRequest req, SlashCommandContext ctx) throws IOException, SlackApiException {
        getApp().getClient()
                .viewsOpen(ViewsOpenRequest
                        .builder()
                        .triggerId(req.getPayload().getTriggerId())
                        .token(ctx.getBotToken())
                        .view(getViews().getBookingRequestView(accountType))
                        .build()
                );
        return ctx.ack();
    }
}
