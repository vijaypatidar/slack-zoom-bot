package com.consultadd.slackzoom.slack.view.submisions;

import com.consultadd.slackzoom.slack.view.AbstractRegistrableComponent;
import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.ViewSubmissionHandler;

public abstract class AbstractViewSubmissionHandler extends AbstractRegistrableComponent implements ViewSubmissionHandler {
    @Override
    public void register(App app) {
        app.viewSubmission(getCallbackId(),this);
    }

    abstract String getCallbackId();
}
