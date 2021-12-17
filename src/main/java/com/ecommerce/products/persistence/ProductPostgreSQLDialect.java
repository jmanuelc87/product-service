package com.ecommerce.products.persistence;

import org.hibernate.dialect.PostgreSQL10Dialect;

public class ProductPostgreSQLDialect extends PostgreSQL10Dialect {

    public final static String SEARCH_FUNCTION = "search";

    public ProductPostgreSQLDialect() {
        registerFunction(SEARCH_FUNCTION, new PostgreSQLSearchFunction());
    }
}
