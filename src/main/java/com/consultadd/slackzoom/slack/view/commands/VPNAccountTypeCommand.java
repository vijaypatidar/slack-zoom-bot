package com.consultadd.slackzoom.slack.view.commands;

import com.consultadd.slackzoom.enums.AccountType;
import org.springframework.stereotype.Component;

@Component
public class VPNAccountTypeCommand extends AbstractAccountTypeCommand {
    public VPNAccountTypeCommand() {
        super(AccountType.VPN);
    }
}
