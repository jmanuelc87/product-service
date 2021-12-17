package com.ecommerce.products.controller;

import com.ecommerce.products.persistence.Product;
import com.ecommerce.products.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService service) {
        this.productService = service;
    }

    @PostMapping(value = "products", consumes = "application/json")
    public ResponseEntity<Long> createProduct(
            @RequestBody Product product
    ) {
        var result = productService.addNewProduct(product);

        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "products/search", produces = "application/json")
    public ResponseEntity<Page<Product>> searchProduct(@RequestParam(name = "q") String searchQuery,
                                                       @RequestParam(name = "attr", required = false) String attributesQuery,
                                                       @RequestParam(name = "c", required = false) Long category,
                                                       Pageable pageable) {
        var productList = productService.searchProducts(searchQuery, attributesQuery, category, pageable);
        return ResponseEntity.ok(productList);
    }
}
