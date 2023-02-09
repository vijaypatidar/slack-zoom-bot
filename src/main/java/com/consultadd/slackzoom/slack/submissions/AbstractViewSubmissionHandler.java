package com.consultadd.slackzoom.slack.submissions;

import com.consultadd.slackzoom.slack.AbstractRegistrableComponent;
import com.slack.api.bolt.App;
import com.slack.api.bolt.handler.builtin.ViewSubmissionHandler;
import java.util.regex.Pattern;

public abstract class AbstractViewSubmissionHandler extends AbstractRegistrableComponent implements ViewSubmissionHandler {
    @Override
    public void register(App app) {
        app.viewSubmission(Pattern.compile(getCallbackIdRegex()), this);
    }

    abstract String getCallbackIdRegex();
}
