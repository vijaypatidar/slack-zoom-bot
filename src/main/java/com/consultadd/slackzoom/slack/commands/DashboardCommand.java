package com.consultadd.slackzoom.slack.commands;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DashboardCommand extends AbstractSlashCommandHandler {
    @Override
    public String getCommandName() {
        return "dashboard";
    }

    @Override
    public Response apply(SlashCommandRequest req, SlashCommandContext ctx) throws IOException, SlackApiException {
        getApp().getClient()
                .viewsOpen(ViewsOpenRequest
                        .builder()
                        .triggerId(req.getPayload().getTriggerId())
                        .token(ctx.getBotToken())
                        .view(getViews().getAccountDashboard(req.getPayload().getUserId()))
                        .build()
                );
        return ctx.ack();
    }
}
