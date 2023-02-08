package com.consultadd.slackzoom.slack.view.actions;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class TimeDateBlockAction extends AbstractBlockActionHandler {
    @Override
    public Response apply(BlockActionRequest req, ActionContext ctx) {
        return ctx.ack();
    }

    @Override
    String getActionName() {
        return "";
    }

    @Override
    public void register(App app) {
        app.blockAction(Pattern.compile(".*Time"), this);
        app.blockAction(Pattern.compile(".*Date"), this);
    }
}
