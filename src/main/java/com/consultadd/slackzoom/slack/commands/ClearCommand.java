package com.consultadd.slackzoom.slack.commands;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatDeleteRequest;
import com.slack.api.methods.response.conversations.ConversationsHistoryResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class ClearCommand extends AbstractSlashCommandHandler {

    @Override
    public String getCommandName() {
        return "clear";
    }

    @Override
    public Response apply(SlashCommandRequest req, SlashCommandContext ctx) throws IOException, SlackApiException {
        ConversationsHistoryResponse conversationsHistoryResponse = getApp().getClient()
                .conversationsHistory(builder -> builder.channel(ctx.getChannelId()).token(ctx.getBotToken()));
        conversationsHistoryResponse.getMessages().parallelStream().forEach(message -> {
            try {
                getApp().getClient().chatDelete(ChatDeleteRequest.builder().channel(ctx.getChannelId()).token(ctx.getBotToken()).ts(message.getTs()).build());
            } catch (Exception e) {
                log.error("Error while deleting message", e);
            }
        });
        return ctx.ack();
    }
}
