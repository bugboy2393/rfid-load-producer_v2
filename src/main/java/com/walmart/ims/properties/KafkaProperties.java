package com.walmart.ims.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Data
@Component("kafkaProperties")
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "kafka")
@Getter
@Setter
@Validated
public class KafkaProperties {
    @NotNull
    @NotEmpty
    private String broker;

    @NotNull
    @NotEmpty
    private String topic;

    @Positive
    private int recordsPerSecond;

    @Positive
    private int producerRequestTimeoutMs;

    @Positive
    private int producerMaxInFlightRequestsPerConnection;

    @NotNull
    @NotEmpty
    private String producerAcks;

    @PositiveOrZero
    private Long autoCommitInterval;

    @NotNull
    private boolean useSchemaRegistry;

    @NotNull
    @NotEmpty
    private String schemaRegistryURL;

    @NotNull
    @NotEmpty
    private String autoOffsetReset;

    @Positive
    private Long cacheMaxBytesBuffering;

    @NotNull
    @NotEmpty
    private String applicationId;

    @NotNull
    @NotEmpty
    private String streamsCloseTimeout;

    @NotNull
    @NotEmpty
    private String[] registeredMetrics;

    @NotNull
    @NotEmpty
    private String clientId;
}