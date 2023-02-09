package com.consultadd.slackzoom.slack.shortcuts;

import com.slack.api.bolt.context.builtin.MessageShortcutContext;
import com.slack.api.bolt.request.builtin.MessageShortcutRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class DashBoardShortcut extends AbstractShortcutHandler{
    @Override
    String getCallbackIdRegex() {
        return "dashboard";
    }

    @Override
    public Response apply(MessageShortcutRequest req, MessageShortcutContext ctx) throws IOException, SlackApiException {
        getApp().getClient()
                .viewsOpen(ViewsOpenRequest
                        .builder()
                        .triggerId(req.getPayload().getTriggerId())
                        .token(ctx.getBotToken())
                        .view(getViews().getAccountDashboard(req.getPayload().getUser().getId()))
                        .build()
                );
        return ctx.ack();
    }
}
