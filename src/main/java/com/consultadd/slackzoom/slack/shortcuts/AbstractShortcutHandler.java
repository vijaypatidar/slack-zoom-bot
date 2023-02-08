package com.consultadd.slackzoom.slack.shortcuts;

import com.consultadd.slackzoom.slack.AbstractRegistrableComponent;
import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.MessageShortcutHandler;
import java.util.regex.Pattern;

public abstract class AbstractShortcutHandler extends AbstractRegistrableComponent implements MessageShortcutHandler {
    @Override
    public void register(App app) {
        app.messageShortcut(Pattern.compile("/"+getCallbackIdRegex()), this);
    }

    abstract String getCallbackIdRegex();
}
