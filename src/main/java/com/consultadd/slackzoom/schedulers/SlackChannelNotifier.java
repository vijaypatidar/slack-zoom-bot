package com.consultadd.slackzoom.schedulers;

import com.consultadd.slackzoom.slack.ICDataSource;
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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlackChannelNotifier implements ApplicationListener<ApplicationEvent> {
    private final ICDataSource dataSource;
    private final App app;
    private final AppConfig config;
    private final SlackViews slackViews;
    private final String CHANNEL_ID = "C04HYPHLH25";
    private ChatPostMessageResponse response = null;

    @Scheduled(initialDelay = 5000, fixedDelay = 10000)
    public void updateChannel() throws SlackApiException, IOException {
        if (response == null) {
            ChatPostMessageResponse chatPostMessageResponse = app.getClient().chatPostMessage(req -> req.channel(CHANNEL_ID)
                    .blocks(slackViews.getAccountStatus())
                    .token(config.getSingleTeamBotToken()));
            this.response = chatPostMessageResponse;
            log.error("ChatPostMessageResponse:{}", chatPostMessageResponse);
        } else {
            ChatUpdateResponse chatUpdateResponse = app.getClient().chatUpdate(ChatUpdateRequest.builder()
                    .channel(CHANNEL_ID)
                    .ts(this.response.getTs())
                    .blocks(slackViews.getAccountStatus())
                    .token(config.getSingleTeamBotToken()).build());
            log.error("ChatUpdateResponse:{}", chatUpdateResponse);
        }
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        try {
            updateChannel();
        } catch (SlackApiException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
