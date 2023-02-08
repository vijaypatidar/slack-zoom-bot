package com.consultadd.slackzoom.slack.view.actions;

import com.consultadd.slackzoom.slack.view.AbstractRegistrableComponent;
import com.consultadd.slackzoom.slack.view.RegistrableComponent;
import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.BlockActionHandler;

public abstract class AbstractBlockActionHandler extends AbstractRegistrableComponent implements BlockActionHandler, RegistrableComponent {
    @Override
    public void register(App app) {
        app.blockAction(getActionName(),this);
    }

    abstract String getActionName();
}
