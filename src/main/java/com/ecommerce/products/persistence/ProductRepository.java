package com.ecommerce.products.persistence;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource(path = "products")
public interface ProductRepository extends CrudRepository<Product, Long>, JpaSpecificationExecutor<Product>, ProductRepositoryCustom {
}
