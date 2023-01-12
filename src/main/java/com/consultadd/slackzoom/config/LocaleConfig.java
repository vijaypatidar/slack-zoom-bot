package com.consultadd.slackzoom.config;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocaleConfig {

    @Value(value = "${TIME_ZONE_ID:EST}")
    String timeZone;
    @PostConstruct
    public void setDefaultLocale(){
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

}
