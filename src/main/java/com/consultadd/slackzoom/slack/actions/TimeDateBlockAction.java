package com.consultadd.slackzoom.slack.actions;

import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import org.springframework.stereotype.Component;

@Component
public class TimeDateBlockAction extends AbstractBlockActionHandler {
    @Override
    public Response apply(BlockActionRequest req, ActionContext ctx) {
        return ctx.ack();
    }

    @Override
    String getActionNameRegex() {
        return ".*(Time|Date)";
    }
}
