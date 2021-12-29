package com.ecommerce.products.service;

import com.ecommerce.products.domain.SearchVectorQuery;
import com.ecommerce.products.persistence.Product;
import com.ecommerce.products.persistence.ProductRepository;
import com.ecommerce.products.util.InMemoryCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final InMemoryCache<String, Page<Product>> cache;

    public ProductService(ProductRepository productRepository, InMemoryCache<String, Page<Product>> cache) {
        this.productRepository = productRepository;
        this.cache = cache;
    }

    public Long addNewProduct(Product product) {
        product.getAttributes()
                .forEach(attr -> attr.setProduct(product));
        var savedProduct = productRepository.save(product);
        return savedProduct.getId();
    }

    public Page<Product> searchProducts(String searchQuery, String attributesQuery, Long category, Pageable pageable) {
        var key = searchQuery + "\t" + attributesQuery + "\t" + category + "\t" + pageable.getOffset() + "\t" + pageable.getPageSize();

        return cache.getOrComputeIfAbsent(key, this::searchProducts);
    }

    private Page<Product> searchProducts(String key) {
        String[] splitted = key.split("\t");

        return productRepository.searchByQueryAndCategoryIfPresent(
                SearchVectorQuery.of(splitted[0], splitted[1]),
                Long.parseLong(splitted[2]),
                PageRequest.of(Integer.parseInt(splitted[3]),
                        Integer.parseInt(splitted[4])));
    }
}
