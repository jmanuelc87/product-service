package com.ecommerce.products.configuration;

import com.ecommerce.products.persistence.Category;
import com.ecommerce.products.persistence.Product;
import com.ecommerce.products.persistence.ProductAttribute;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class RepositoryConfiguration implements RepositoryRestConfigurer {

    private final HttpMethod[] methods = {HttpMethod.DELETE, HttpMethod.PUT, HttpMethod.PATCH};

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.setReturnBodyForPutAndPost(true);

        config.getExposureConfiguration()
                .forDomainType(Category.class)
                .withItemExposure((metdata, httpMethods) -> httpMethods.disable(methods));

        config.getExposureConfiguration()
                .forDomainType(ProductAttribute.class)
                .withItemExposure((metdata, httpMethods) -> httpMethods.disable(methods));

        config.getExposureConfiguration()
                .forDomainType(Product.class)
                .withItemExposure((metdata, httpMethods) -> httpMethods.disable(methods));
    }
}
