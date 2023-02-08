package com.consultadd.slackzoom.slack.view.commands;

import com.consultadd.slackzoom.slack.view.AbstractRegistrableComponent;
import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.SlashCommandHandler;

public abstract class AbstractSlashCommandHandler extends AbstractRegistrableComponent implements SlashCommandHandler {

    public abstract String getCommandName();
    @Override
    public void register(App app) {
        app.command("/"+getCommandName(),this);
    }
}
