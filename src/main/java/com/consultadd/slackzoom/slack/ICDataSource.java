package com.consultadd.slackzoom.slack;


import com.consultadd.slackzoom.models.Booking;
import com.consultadd.slackzoom.models.ZoomAccount;
import com.consultadd.slackzoom.services.ZoomAccountService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.model.view.ViewState;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ICDataSource {
    @NonNull
    private final ZoomAccountService zoomAccountService;
    @NonNull
    private final ObjectMapper objectMapper;

    public Map<String, Object> getAllZoomAccounts() {
        return Map.of("options",
                zoomAccountService
                        .getAllAccounts()
                        .stream().map(this::toMap)
                        .toList()
        );
    }

    private Map<String, Object> toMap(ZoomAccount zoomAccount) {

        return Map.of(
                "value", zoomAccount.getAccountId(),
                "text", Map.of(
                        "type", "plain_text",
                        "text", zoomAccount.getAccountName()
                )
        );
    }

    public Optional<ZoomAccount> bookAvailableAccount(Map<String, ViewState.Value> state, String userId) throws JsonProcessingException {
        log.info("ViewState:" + objectMapper.writeValueAsString(state));
        int startTime = toIntTime(state.get("startTime").getSelectedTime());
        int endTime = toIntTime(state.get("endTime").getSelectedTime());
        List<ZoomAccount> availableAccounts = zoomAccountService.findAvailableAccounts(startTime, endTime);
        if (availableAccounts.isEmpty()) {
            return Optional.empty();
        } else {
            ZoomAccount account = availableAccounts.get(0);
            Booking booking = new Booking(startTime, endTime, userId, UUID.randomUUID().toString(), account.getAccountId());
            zoomAccountService.bookAccount(booking);
            return Optional.of(account);
        }
    }

    private int toIntTime(String time) {
        String[] times = time.split(":");
        return Integer.parseInt(times[0]) * 60 + Integer.parseInt(times[1]);
    }
}
