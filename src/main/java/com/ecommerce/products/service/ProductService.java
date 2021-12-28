package com.ecommerce.products.service;

import com.ecommerce.products.domain.SearchVectorQuery;
import com.ecommerce.products.persistence.Product;
import com.ecommerce.products.persistence.ProductRepository;
import com.ecommerce.products.util.LRUCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final LRUCache<String, Page<Product>> cache;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.cache = new LRUCache<>(500);
    }

    public Long addNewProduct(Product product) {
        product.getAttributes()
                .forEach(attr -> attr.setProduct(product));
        var savedProduct = productRepository.save(product);
        return savedProduct.getId();
    }

    public Page<Product> searchProducts(String searchQuery, String attributesQuery, Long category, Pageable pageable) {
        String cacheKey = searchQuery + "@" + attributesQuery + "@" + category + "@" + pageable.getOffset() + "@" + pageable.getPageSize();
        Optional<Page<Product>> optional =
                cache.getOrEmpty(cacheKey);
        if (optional.isEmpty()) {
            Page<Product> page = productRepository.searchByQueryAndCategoryIfPresent(SearchVectorQuery.of(searchQuery, attributesQuery), category, pageable);
            cache.put(cacheKey, page);
            optional = Optional.of(page);
        }

        return optional.get();
    }
}
