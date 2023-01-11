package com.consultadd.slackzoom.services;

import java.util.Optional;

public interface ToolsBotConfigService {
    Optional<String> getTsForChannel(String channelId);

    void saveTsForChannel(String channelId, String ts);
}
