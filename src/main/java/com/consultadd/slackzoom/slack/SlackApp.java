package com.consultadd.slackzoom.slack;

import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import com.consultadd.slackzoom.services.ZoomAccountService;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.view.ViewState;
import java.util.HashMap;
import java.util.List;
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
    private final ZoomAccountService accountService;
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
            Optional<Booking> optionalBooking = accountService.bookAvailableAccount(state, req.getPayload().getUser().getId());
            if (optionalBooking.isPresent()) {
                Booking booking = optionalBooking.get();
                ZoomAccount account = accountService.getAccount(booking.getAccountId());
                String text = String.format(
                        "You can use this account from %s to %s EST.%n```%s%nUsername: %s%nPassword: %s```%n Please update the account state to available, if it get free before the expected end time or if not need anymore.",
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
                                        .blocks(List.of(SectionBlock
                                                .builder()
                                                .text(MarkdownTextObject
                                                        .builder()
                                                        .text(text)
                                                        .build())
                                                .accessory(ButtonElement
                                                        .builder()
                                                        .style(SlackViews.DANGER)
                                                        .text(PlainTextObject
                                                                .builder()
                                                                .text("Free Account")
                                                                .build())
                                                        .actionId(SlackViews.ACTION_RELEASE_BOOKED_ACCOUNT)
                                                        .value(booking.getBookingId())
                                                        .build())
                                                .build()))
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

        app.blockAction(SlackViews.ACTION_RELEASE_BOOKED_ACCOUNT, (req, ctx) -> {
            String bookingId = req.getPayload()
                    .getActions()
                    .stream()
                    .filter(action -> action.getActionId().equals(SlackViews.ACTION_RELEASE_BOOKED_ACCOUNT))
                    .findAny()
                    .orElseThrow()
                    .getValue();
            accountService.deleteBooking(bookingId);
            applicationEventPublisher.publishEvent(new AccountStatusChangeEvent(this));
            app.getClient()
                    .chatPostMessage(builder -> builder
                            .text("Thank you! Account status has been changed to available.")
                            .channel(req.getPayload().getUser().getId())
                            .token(ctx.getBotToken()));
            return ctx.ack();
        });

        app.blockAction(SlackViews.ACTION_BOOK_ACCOUNT_REQUEST, (req, ctx) -> {
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
