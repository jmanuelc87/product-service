package com.ecommerce.products.configuration;

import com.ecommerce.products.persistence.Product;
import com.ecommerce.products.util.InMemoryCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

@Configuration
public class InMemoryCacheConfiguration {

    private final InMemoryCacheProperties properties;

    public InMemoryCacheConfiguration(InMemoryCacheProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "close")
    public InMemoryCache<String, Page<Product>> productsInMemoryCache() {
        return new InMemoryCache<>(properties.BETA, properties.TTL, properties.SLEEP_INTERVAL, properties.MAX_ITEMS);
    }
}
