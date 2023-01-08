package com.consultadd.slackzoom.slack;

import com.consultadd.slackzoom.models.ZoomAccount;
import com.consultadd.slackzoom.services.ZoomAccountService;
import com.slack.api.model.block.DividerBlock;
import com.slack.api.model.block.InputBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.block.composition.PlainTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.MultiStaticSelectElement;
import com.slack.api.model.block.element.TimePickerElement;
import com.slack.api.model.view.View;
import com.slack.api.model.view.ViewClose;
import com.slack.api.model.view.ViewSubmit;
import com.slack.api.model.view.ViewTitle;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SlackViews {
    public final static String MODAL_VIEW = "modal";
    public final static String PLAIN_TEXT = "plain_text";
    public final static String SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK = "find-zoom-account";

    private final ZoomAccountService accountService;

    public View getZoomRequestModal() {
        ViewTitle title = ViewTitle.builder()
                .type(PLAIN_TEXT)
                .text("Zoom pro account")
                .build();
        ViewSubmit submit = ViewSubmit.builder()
                .type(PLAIN_TEXT)
                .text("Submit")
                .build();
        ViewClose close = ViewClose.builder()
                .type(PLAIN_TEXT)
                .text("Cancel")
                .build();

        List<LayoutBlock> blocks = new LinkedList<>();

        blocks.add(SectionBlock.builder()
                .text(PlainTextObject.builder()
                        .text("Please select the duration for which you need pro zoom account.")
                        .build())
                .build());

        blocks.add(InputBlock.builder()
                .element(TimePickerElement
                        .builder()
                        .actionId("startTime")
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select start time")
                                .build())
                        .build())
                .label(PlainTextObject
                        .builder()
                        .text("Start time")
                        .build())
                .build());

        blocks.add(InputBlock.builder()
                .element(TimePickerElement
                        .builder()
                        .actionId("endTime")
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select end time")
                                .build())
                        .build())
                .label(PlainTextObject
                        .builder()
                        .text("End time")
                        .build())
                .build());

        List<OptionObject> options = accountService.getAllAccounts()
                .stream()
                .map(zoomAccount -> OptionObject
                        .builder()
                        .value(zoomAccount.getAccountId())
                        .text(PlainTextObject
                                .builder()
                                .text(zoomAccount.getAccountName())
                                .build())
                        .build())
                .toList();

        blocks.add(InputBlock.builder()
                .optional(true)
                .element(MultiStaticSelectElement
                        .builder()
                        .actionId("preferred_accounts")
                        .placeholder(PlainTextObject
                                .builder()
                                .text("Select zoom account")
                                .build())
                        .options(options)
                        .build())
                .label(PlainTextObject
                        .builder()
                        .text("Preferred zoom account")
                        .build())
                .build());

        return View.builder()
                .type(MODAL_VIEW)
                .callbackId(SAVE_FIND_AND_BOOK_ACCOUNT_CALLBACK)
                .title(title)
                .submit(submit)
                .close(close)
                .blocks(blocks)
                .build();
    }

    public List<LayoutBlock> getAccountStatus() {
        List<LayoutBlock> blocks = new LinkedList<>();
        blocks.add(SectionBlock
                .builder()
                .text(PlainTextObject
                        .builder()
                        .text("Zoom accounts status")
                        .build())
                .build());

        blocks.add(DividerBlock.builder().build());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EST"));
        int start = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        Set<String> availableAccounts = accountService
                .findAvailableAccounts(start, start)
                .stream()
                .map(ZoomAccount::getAccountId)
                .collect(Collectors.toSet());
        AtomicInteger count = new AtomicInteger(1);
        accountService.getAllAccounts().forEach(zoomAccount -> {
            SectionBlock.SectionBlockBuilder builder = SectionBlock.builder();
            StringBuilder textBuilder = new StringBuilder();
            textBuilder
                    .append("`")
                    .append(count.getAndIncrement())
                    .append(".` *")
                    .append(zoomAccount.getAccountName())
                    .append("*\n");
            if (availableAccounts.contains(zoomAccount.getAccountId())) {
                textBuilder.append("Available");
                builder = builder.accessory(ButtonElement
                        .builder()
                        .actionId("use_account-" + zoomAccount.getAccountId())
                        .text(PlainTextObject.builder().text("USE").build())
                        .style("primary")
                        .build());
            } else {
                textBuilder.append("In use");
            }
            builder = builder.text(MarkdownTextObject
                    .builder()
                    .text(textBuilder.toString())
                    .build());
            blocks.add(builder.build());
        });

        return blocks;
    }

    @Deprecated
    public String getModal() {
        try {
            ClassPathResource json = new ClassPathResource("action.json");
            BufferedInputStream bis = new BufferedInputStream(json.getInputStream());
            StringBuilder sb = new StringBuilder();
            byte[] bytes = new byte[1024];
            int read = -1;
            while ((read = bis.read(bytes)) > 0) {
                sb.append(new String(bytes, 0, read));
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }
}
