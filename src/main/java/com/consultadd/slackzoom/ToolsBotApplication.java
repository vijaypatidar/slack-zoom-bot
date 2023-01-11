package com.consultadd.slackzoom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
public class ToolsBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolsBotApplication.class, args);
    }

}
