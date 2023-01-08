package com.consultadd.slackzoom.slack;

import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatUpdateRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import java.io.IOException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlackChannelNotifier {
    private ChatPostMessageResponse response = null;
    private final ICDataSource dataSource;
    private final App app;
    private final AppConfig config;

    @Scheduled(initialDelay = 5000,fixedDelay = 5000)
    public void updateChannel() throws SlackApiException, IOException {
        if (response==null){
            ChatPostMessageResponse chatPostMessageResponse = app.getClient().chatPostMessage(req -> req.channel("U04HL4VKQ9G")
                    .text(dataSource.getAllZoomAccounts().toString()+"\nTime:"+ new Date())
                    .mrkdwn(true)
                    .token(config.getSingleTeamBotToken()));
            this.response = chatPostMessageResponse;
        }else{
            ChatUpdateResponse chatUpdateResponse = app.getClient().chatUpdate(ChatUpdateRequest.builder()
                    .channel(this.response.getChannel())
                    .ts(this.response.getTs())
                    .text(dataSource.getAllZoomAccounts().toString() + "\nTime:" + new Date())
                    .token(config.getSingleTeamBotToken()).build());
        }
    }
}
