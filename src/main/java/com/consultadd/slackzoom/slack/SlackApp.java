package com.consultadd.slackzoom.slack;

import com.consultadd.slackzoom.enums.AccountType;
import com.consultadd.slackzoom.events.AccountStatusChangeEvent;
import com.consultadd.slackzoom.models.Account;
import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.BookingRequest;
import com.consultadd.slackzoom.services.AccountService;
import com.consultadd.slackzoom.services.BookingService;
import com.consultadd.slackzoom.utils.DateTimeUtils;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.views.response.ViewSubmissionResponse;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.handler.builtin.ViewSubmissionHandler;
import com.slack.api.methods.request.chat.ChatDeleteRequest;
import com.slack.api.methods.request.views.ViewsOpenRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import com.slack.api.methods.response.views.ViewsUpdateResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewState;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static com.consultadd.slackzoom.services.impls.DynamoDbAccountService.*;
import static com.consultadd.slackzoom.services.impls.DynamoDbBookingService.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SlackApp implements ApplicationEventPublisherAware {
    private final SlackViews slackViews;
    private final AccountService accountService;
    private final BookingService bookingService;
    private ApplicationEventPublisher applicationEventPublisher;

    private static void handleTimeBlocksEvent(App app) {
        app.blockAction(Pattern.compile(".*Time"), (req, context) -> context.ack());
        app.blockAction(Pattern.compile(".*Date"), (req, context) -> context.ack());
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
    public AppConfig loadSingleWorkspaceAppConfig() {
        return AppConfig.builder()
                .singleTeamBotToken(System.getenv("SLACK_BOT_TOKEN"))
                .signingSecret(System.getenv("SLACK_SIGNING_SECRET"))
                .build();
    }

    @Bean
    public App initSlackApp(AppConfig config) {
        App app = new App(config);
        buildAccountTypesCommand(app);
        buildClearCommand(app);
        buildAddUpdateAccountDetailAction(app);
        buildAccountDashboardCommand(app);
        handleTimeBlocksEvent(app);
        buildSaveViewCallbacks(app);
        buildAddUpdateAccountViewCallbacks(app);
        buildFreeAccountBlockAction(app);
        handleBookAccountAction(app);
        return app;
    }

    private void buildAccountDashboardCommand(App app) {
        app.command("/dashboard", (req, ctx) -> {
            app.getClient()
                    .viewsOpen(ViewsOpenRequest
                            .builder()
                            .triggerId(req.getPayload().getTriggerId())
                            .token(ctx.getBotToken())
                            .view(slackViews.getAccountDashboard(req.getPayload().getUserId()))
                            .build()
                    );
            return ctx.ack();
        });
    }

    private void buildAddUpdateAccountViewCallbacks(App app) {
        app.viewSubmission(
                Pattern.compile(SlackViews.ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK + ".*"),
                handleAddUpdateAccountViewSubmission(app)
        );
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
                            .view(slackViews.getBookingRequestView(AccountType.valueOf(accountType)))
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
            bookingService.delete(bookingId);
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

    private void buildAccountTypesCommand(App app) {
        for (AccountType accountType : AccountType.values()) {
            app.command("/" + accountType.getType(), (req, ctx) -> {
                app.getClient()
                        .viewsOpen(ViewsOpenRequest
                                .builder()
                                .triggerId(req.getPayload().getTriggerId())
                                .token(ctx.getBotToken())
                                .view(slackViews.getBookingRequestView(accountType))
                                .build()
                        );
                return ctx.ack();
            });
        }
    }

    private void buildAddUpdateAccountDetailAction(App app) {
        app.blockAction(SlackViews.ACTION_EDIT_ACCOUNT_DETAIL, (req, ctx) -> {
            Account account = req.getPayload().getActions()
                    .stream()
                    .filter(action -> SlackViews.ACTION_EDIT_ACCOUNT_DETAIL.equals(action.getActionId()))
                    .findAny()
                    .map(BlockActionPayload.Action::getValue)
                    .map(accountService::getAccountById)
                    .orElse(null);
            ViewsUpdateResponse viewsUpdateResponse = app.getClient()
                    .viewsUpdate(builder -> builder
                            .token(ctx.getBotToken())
                            .view(slackViews.addUpdateAccountDetailView(account))
                            .viewId(req.getPayload().getView().getId()));
            if (!viewsUpdateResponse.isOk()) {
                log.error("viewsUpdateResponse:{}", viewsUpdateResponse);
            }
            return ctx.ack();
        });
    }

    @NotNull
    private ViewSubmissionHandler handleAccountRequestViewSubmission(App app, AccountType accountType) {
        return (req, ctx) -> {
            Map<String, ViewState.Value> state = new HashMap<>();
            req.getPayload().getView().getState().getValues().values().forEach(state::putAll);
            LocalTime startTime = DateTimeUtils.stringToLocalTime(state.get(START_TIME).getSelectedTime());
            LocalTime endTime = DateTimeUtils.stringToLocalTime(state.get(END_TIME).getSelectedTime());
            LocalDate bookingDate = DateTimeUtils.stringToDate(state.get(BOOKING_DATE).getSelectedDate());

            BookingRequest bookingRequest = BookingRequest.builder()
                    .userId(req.getPayload().getUser().getId())
                    .accountType(accountType)
                    .startTime(startTime)
                    .endTime(endTime)
                    .bookingDate(bookingDate)
                    .build();

            Optional<Booking> optionalBooking = accountService.bookAvailableAccount(bookingRequest);
            if (optionalBooking.isPresent()) {
                Booking booking = optionalBooking.get();
                this.applicationEventPublisher.publishEvent(new AccountStatusChangeEvent(this));
                List<LayoutBlock> blocks = slackViews.getAccountBookedResponseMessageBlocks(booking);
                app.getClient()
                        .chatPostMessage(msg ->
                                msg.channel(req.getPayload().getUser().getId())
                                        .token(ctx.getBotToken())
                                        .blocks(blocks)
                        );
            } else {
                return ctx.ackWithErrors(Map.of("endTimeBlock", "There is no account available for this time frame."));
            }
            return ctx.ack();
        };
    }


    @NotNull
    private ViewSubmissionHandler handleAddUpdateAccountViewSubmission(App app) {
        return (req, ctx) -> {
            Map<String, ViewState.Value> state = new HashMap<>();
            req.getPayload().getView().getState().getValues().values().forEach(state::putAll);
            String callbackId = req.getPayload().getView().getCallbackId();
            log.error("callbackId:{}", callbackId);
            String accountId;
            if (!SlackViews.ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK.equals(callbackId)) {
                accountId = callbackId.replace(SlackViews.ADD_UPDATE_ACCOUNT_ACCOUNT_CALLBACK, "");
            } else {
                accountId = UUID.randomUUID().toString();
            }
            String accountName = state.get(ACCOUNT_NAME).getValue();
            AccountType accountType = AccountType.valueOf(state.get(ACCOUNT_TYPE).getSelectedOption().getValue());
            String accountUsername = state.get(USERNAME).getValue();
            String accountPassword = state.get(PASSWORD).getValue();
            Account newAccount = Account.builder()
                    .accountType(accountType)
                    .accountName(accountName)
                    .accountId(accountId)
                    .ownerId(req.getPayload().getUser().getId())
                    .username(accountUsername)
                    .password(accountPassword)
                    .build();
            accountService.save(newAccount);
            applicationEventPublisher.publishEvent(new AccountStatusChangeEvent(this));

            View accountDashboard = slackViews.getAccountDashboard(req.getPayload().getUser().getId());

            return ctx.ack(ViewSubmissionResponse.builder()
                    .view(accountDashboard)
                    .responseAction("update").build());
        };
    }


    @Override
    public void setApplicationEventPublisher(@NotNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
