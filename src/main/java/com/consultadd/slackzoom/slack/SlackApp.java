package com.consultadd.slackzoom.slack;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.services.AccountService;
import com.consultadd.slackzoom.services.BookingService;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.handler.builtin.ViewSubmissionHandler;
import com.slack.api.methods.request.chat.ChatDeleteRequest;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SlackApp implements ApplicationEventPublisherAware {
    private final SlackViews slackViews;
    private final AccountService accountService;
    private final BookingService bookingService;
    private ApplicationEventPublisher applicationEventPublisher;

    private static void handleTimeBlocksEvent(App app) {
        app.blockAction(".Time", (req, context) -> context.ack());
    }

    private static void buildClearCommand(App app) {
        app.command("/clear", (req, ctx) -> {
            ConversationsHistoryResponse conversationsHistoryResponse = app.getClient()
                    .conversationsHistory(builder -> builder.channel(ctx.getChannelId()).token(ctx.getBotToken()));
            conversationsHistoryResponse.getMessages().parallelStream().forEach(message -> {
                try {
                    app.getClient().chatDelete(ChatDeleteRequest.builder().channel(ctx.getChannelId()).token(ctx.getBotToken()).ts(message.getTs()).build());
                } catch (Exception e) {
                    log.error("Error while deleting message", e);
                }
            });
            return ctx.ack();
        });
    }

    @Bean
    public App initSlackApp(AppConfig config) {
        App app = new App(config);
        buildAccountsCommand(app);
        buildClearCommand(app);
        handleTimeBlocksEvent(app);
        buildSaveViewCallbacks(app);
        buildFreeAccountBlockAction(app);
        handleBookAccountAction(app);
        return app;
    }

    @Bean
    public AppConfig loadSingleWorkspaceAppConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(System.getenv("SLACK_BOT_TOKEN"))
                .signingSecret(System.getenv("SLACK_SIGNING_SECRET"))
                .build();
    }

    private void handleBookAccountAction(App app) {
        app.blockAction(SlackViews.ACTION_BOOK_ACCOUNT_REQUEST, (req, ctx) -> {
            String accountType = req.getPayload()
                    .getActions()
                    .stream()
                    .filter(action -> action.getActionId().equals(SlackViews.ACTION_BOOK_ACCOUNT_REQUEST))
                    .findAny()
                    .orElseThrow()
                    .getValue();
            app.getClient()
                    .viewsOpen(ViewsOpenRequest
                            .builder()
                            .triggerId(req.getPayload().getTriggerId())
                            .token(ctx.getBotToken())
                            .view(slackViews.getRequestModal(AccountType.valueOf(accountType)))
                            .build()
                    );
            return ctx.ack();
        });
    }

    private void buildFreeAccountBlockAction(App app) {
        app.blockAction(SlackViews.ACTION_RELEASE_BOOKED_ACCOUNT, (req, ctx) -> {
            String bookingId = req.getPayload()
                    .getActions()
                    .stream()
                    .filter(action -> action.getActionId().equals(SlackViews.ACTION_RELEASE_BOOKED_ACCOUNT))
                    .findAny()
                    .orElseThrow()
                    .getValue();
            bookingService.deleteBooking(bookingId);
            applicationEventPublisher.publishEvent(new AccountStatusChangeEvent(this));
            app.getClient()
                    .chatPostMessage(builder -> builder
                            .text("Thank you! Account status has been changed to available.")
                            .channel(req.getPayload().getUser().getId())
                            .token(ctx.getBotToken()));
            return ctx.ack();
        });
    }

    private void buildSaveViewCallbacks(App app) {
        for (AccountType accountType : AccountType.values()) {
            app.viewSubmission(
                    SlackViews.SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK + ":" + accountType.getType(),
                    handleAccountRequestViewSubmission(app, accountType)
            );
        }
    }

    private void buildAccountsCommand(App app) {
        app.command("/accounts", (req, ctx) -> {
            app.getClient()
                    .viewsOpen(ViewsOpenRequest
                            .builder()
                            .triggerId(req.getPayload().getTriggerId())
                            .token(ctx.getBotToken())
                            .view(slackViews.getRequestModal(AccountType.ZOOM))
                            .build()
                    );
            return ctx.ack();
        });
    }

    @NotNull
    private ViewSubmissionHandler handleAccountRequestViewSubmission(App app, AccountType accountType) {
        return (req, ctx) -> {
            Map<String, ViewState.Value> state = new HashMap<>();
            req.getPayload().getView().getState().getValues().values().forEach(state::putAll);
            Optional<Booking> optionalBooking = accountService.bookAvailableAccount(state, req.getPayload().getUser().getId(), accountType);
            if (optionalBooking.isPresent()) {
                Booking booking = optionalBooking.get();
                Account account = accountService.getAccount(booking.getAccountId(), accountType);
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
                return ctx.ackWithErrors(Map.of("endTimeBlock", "There is no account available for this time frame."));
            }
            return ctx.ack();
        };
    }


    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
