package com.ecommerce.products.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Cacheable
@Cache(region = "attributeCache", usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "ecommerce_attributes")
public class ProductAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_attribute_id_seq")
    @SequenceGenerator(name = "product_attribute_id_seq", sequenceName = "ecommerce_attributes_id_seq", allocationSize = 1)
    private Long id;

    private String attribute;

    private String value;

    @JsonIgnore
    @Column(name = "attribute_value_ts", updatable = false, insertable = false)
    private String attributeSearch;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;
}
