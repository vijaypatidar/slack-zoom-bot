package com.consultadd.slackzoom.slack.actions;

import com.consultadd.slackzoom.slack.AbstractRegistrableComponent;
import com.consultadd.slackzoom.slack.RegistrableComponent;
import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.BlockActionHandler;
import java.util.regex.Pattern;

public abstract class AbstractBlockActionHandler extends AbstractRegistrableComponent implements BlockActionHandler, RegistrableComponent {
    @Override
    public void register(App app) {
        app.blockAction(Pattern.compile(getActionNameRegex()), this);
    }

    abstract String getActionNameRegex();
}
