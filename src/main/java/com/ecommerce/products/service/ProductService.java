package com.ecommerce.products.service;

import com.ecommerce.products.domain.SearchVectorQuery;
import com.ecommerce.products.persistence.Product;
import com.ecommerce.products.persistence.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Long addNewProduct(Product product) {
        product.getAttributes()
                .forEach(attr -> attr.setProduct(product));
        var savedProduct = productRepository.save(product);
        return savedProduct.getId();
    }

    public Page<Product> searchProducts(String searchQuery, String attributesQuery, Long category, Pageable pageable) {
        return productRepository.searchByQueryAndCategoryIfPresent(SearchVectorQuery.of(searchQuery, attributesQuery), category, pageable);
    }
}
