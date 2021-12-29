package com.ecommerce.products.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "cache.memory")
public class InMemoryCacheProperties {

    public Duration TTL;

    public Duration SLEEP_INTERVAL;

    public Integer BETA;

    public Integer MAX_ITEMS;
}
