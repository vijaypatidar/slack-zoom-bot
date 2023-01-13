package com.consultadd.slackzoom.schedulers;

import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.consultadd.slackzoom.slack.SlackViews;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatUpdateRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.methods.response.chat.ChatUpdateResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ToolsBotEventListener implements ApplicationListener<ApplicationEvent> {
    private final App app;
    private final AppConfig config;
    private final SlackViews slackViews;
    @Value(value = "${BOT_UPDATE_CHANNEL_ID}")
    String botUpdateChannelId;
    private ChatPostMessageResponse response = null;

    @Scheduled(initialDelay = 1000, fixedDelay = 30000)
    public void updateChannel() throws SlackApiException, IOException {
        if (response == null) {
            ChatPostMessageResponse chatPostMessageResponse = app.getClient()
                    .chatPostMessage(req -> req.channel(botUpdateChannelId)
                            .blocks(slackViews.getAccountStatusMessageView())
                            .token(config.getSingleTeamBotToken()));
            if (chatPostMessageResponse.isOk()) {
                this.response = chatPostMessageResponse;
            } else {
                log.error("ChatPostMessageResponse:{}", chatPostMessageResponse);
            }
        } else {
            ChatUpdateResponse chatUpdateResponse = app.getClient()
                    .chatUpdate(ChatUpdateRequest.builder()
                            .channel(botUpdateChannelId)
                            .ts(this.response.getTs())
                            .blocks(slackViews.getAccountStatusMessageView())
                            .token(config.getSingleTeamBotToken()).build());
            if (!chatUpdateResponse.isOk()) {
                log.error("ChatUpdateResponse:{}", chatUpdateResponse);
            }
        }
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        if (event instanceof AccountStatusChangeEvent) {
            try {
                updateChannel();
            } catch (SlackApiException | IOException e) {
                log.error("Error while processing account status change event", e);
            }
        }
    }
}
