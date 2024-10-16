package com.consultadd.slackzoom.slack;

import com.slack.api.bolt.App;

public interface RegistrableComponent {
    void register(App app);

    void init(App app);
}
