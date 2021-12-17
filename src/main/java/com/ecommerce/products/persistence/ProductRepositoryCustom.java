package com.ecommerce.products.persistence;

import com.ecommerce.products.domain.SearchVectorQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> searchByQueryAndCategoryIfPresent(SearchVectorQuery searchVector, Long category, Pageable pageable);
}
