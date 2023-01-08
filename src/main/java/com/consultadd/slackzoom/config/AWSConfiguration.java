package com.consultadd.slackzoom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Component
public class AWSConfiguration {

    private final Region AWS_REGION = Region.AP_SOUTH_1;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return DefaultCredentialsProvider.create();
    }


    @Bean
    public DynamoDbClient client(AwsCredentialsProvider credentialsProvider) {
        return DynamoDbClient
                .builder()
                .region(AWS_REGION)
                .credentialsProvider(credentialsProvider)
                .build();
    }
}
