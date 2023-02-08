package com.consultadd.slackzoom.slack.view.commands;

import com.consultadd.slackzoom.enums.AccountType;
import org.springframework.stereotype.Component;

@Component
public class GVAccountTypeCommand extends AbstractAccountTypeCommand {
    public GVAccountTypeCommand() {
        super(AccountType.GV);
    }
}
