package com.consultadd.slackzoom.events;

import org.springframework.context.ApplicationEvent;

public class AccountStatusChangeEvent extends ApplicationEvent {
    public AccountStatusChangeEvent(Object source) {
        super(source);
    }
}
