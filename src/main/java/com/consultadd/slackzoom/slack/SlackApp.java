package com.consultadd.slackzoom.slack;

import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.consultadd.slackzoom.models.ZoomAccount;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import com.slack.api.model.view.ViewState;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SlackApp implements ApplicationEventPublisherAware {
    private final SlackViews slackViews;
    private final ICDataSource dataSource;
    Logger logger = LoggerFactory.getLogger(SlackApp.class);
    private ApplicationEventPublisher applicationEventPublisher;

    @Bean
    public App initSlackApp(AppConfig config) {
        App app = new App(config);

        app.command("/accounts", (req, ctx) -> {
            app.getClient()
                    .viewsOpen(ViewsOpenRequest
                            .builder()
                            .triggerId(req.getPayload().getTriggerId())
                            .token(ctx.getBotToken())
                            .view(slackViews.getZoomRequestModal())
                            .build()
                    );
            return ctx.ack();
        });
        app.blockAction(".Time", (req, context) -> context.ack());

        app.viewSubmission("find-zoom-account", (req, ctx) -> {
            Map<String, ViewState.Value> state = new HashMap<>();
            req.getPayload().getView().getState().getValues().values().forEach(state::putAll);
            Optional<ZoomAccount> optionalZoomAccount = dataSource.bookAvailableAccount(state, req.getPayload().getUser().getId());
            if (optionalZoomAccount.isPresent()) {
                ZoomAccount account = optionalZoomAccount.get();
                String text = String.format(
                        "You can use this account from %s to %s.%n```%s%nUsername: %s%nPassword: %s```%n Please update the account state to available, if it get free before the expected end time or if not need anymore.",
                        state.get("startTime").getSelectedTime(),
                        state.get("endTime").getSelectedTime(),
                        account.getAccountName(),
                        account.getUsername(),
                        account.getPassword()
                );
                this.applicationEventPublisher.publishEvent(new AccountStatusChangeEvent(this));
                app.getClient()
                        .chatPostMessage(msg ->
                                msg
                                        .channel(req.getPayload().getUser().getId())
                                        .token(ctx.getBotToken())
                                        .mrkdwn(true)
                                        .text(text)
                        );
            } else {
                app.getClient()
                        .chatPostMessage(msg ->
                                msg
                                        .channel(req.getPayload().getUser().getId())
                                        .token(ctx.getBotToken())
                                        .mrkdwn(true)
                                        .text("All accounts are in use for selected time frame.")
                        );
            }
            return ctx.ack();
        });

        app.message(".", (req, ctx) -> {
            logger.info("ON_MESSAGE:{}", ctx.getChannelId());
            return ctx.ack();
        });
        return app;
    }

    @Bean
    public AppConfig loadSingleWorkspaceAppConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(System.getenv("SLACK_BOT_TOKEN"))
                .signingSecret(System.getenv("SLACK_SIGNING_SECRET"))
                .build();
    }

    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
