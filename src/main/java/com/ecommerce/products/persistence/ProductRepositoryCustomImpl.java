package com.ecommerce.products.persistence;

import com.ecommerce.products.domain.SearchVectorQuery;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.jpa.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final EntityManager em;

    public ProductRepositoryCustomImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<Product> searchByQueryAndCategoryIfPresent(SearchVectorQuery searchVector, Long category, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        var q1 = cb.parameter(String.class);
        var q2 = cb.parameter(String.class);
        var b = cb.parameter(Boolean.class);
        var c = cb.parameter(Long.class);

        Map<String, ParameterExpression<?>> params = Map.of("b", b, "c", c, "q1", q1, "q2", q2);

        TypedQuery<Product> typedQuery = searchQuery(searchVector, category, cb, query, root, params);

        TypedQuery<Long> countQuery = countQuery(searchVector, category, cb, params);

        if (pageable.isPaged()) {
            typedQuery.setFirstResult(Math.toIntExact(pageable.getOffset()))
                    .setMaxResults(pageable.getPageSize());
        }

        typedQuery.setParameter(q1, searchVector.searchQuery());
        typedQuery.setParameter(b, true);

        countQuery.setParameter(q1, searchVector.searchQuery());
        countQuery.setParameter(b, true);

        if (searchVector.attributesQuery().isPresent()) {
            typedQuery.setParameter(q2, searchVector.attributesQuery().get());
            countQuery.setParameter(q2, searchVector.attributesQuery().get());
        }

        if (category != null) {
            typedQuery.setParameter(c, category);
            countQuery.setParameter(c, category);
        }

        var list = typedQuery.getResultList();

        return new PageImpl<>(list, pageable, countQuery.getSingleResult());
    }

    private TypedQuery<Product> searchQuery(SearchVectorQuery searchVector, Long category,
                                            CriteriaBuilder cb, CriteriaQuery<Product> query, Root<Product> root,
                                            Map<String, ParameterExpression<?>> params) {

        createProductSubquery(searchVector, category, params).toPredicate(root, query, cb);

        root.join(Product_.ATTRIBUTES, JoinType.LEFT);
        root.join(Product_.CATEGORIES, JoinType.LEFT);

        EntityGraph<?> eg = em.getEntityGraph("product-entity-graph");

        return em.createQuery(query)
                .setHint("javax.persistence.fetchgraph", eg)
                .setHint(QueryHints.HINT_CACHEABLE, "true")
                .setHint(QueryHints.HINT_CACHE_REGION, "query.product.cache");
    }

    private Specification<Product> createProductSubquery(SearchVectorQuery searchVector, Long category, Map<String, ParameterExpression<?>> params) {
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<Product> prod = sub.from(Product.class);

            Specification<Product> predicate = createCriteriaWhere(searchVector, category, params);
            sub.select(prod.get(Product_.ID));

            Predicate p = predicate.toPredicate(prod, query, cb);

            sub.where(p);

            query.where(cb.in(root.get(Product_.ID)).value(sub));

            return p;
        };
    }

    private TypedQuery<Long> countQuery(SearchVectorQuery searchVector, Long category,
                                        CriteriaBuilder cb, Map<String, ParameterExpression<?>> params) {
        CriteriaQuery<Long> criteriaQuery = cb.createQuery(Long.class);
        Root<Product> productRoot = criteriaQuery.from(Product.class);

        criteriaQuery.select(cb.count(productRoot).alias("count"));

        Specification<Product> predicate = createCriteriaWhere(searchVector, category, params);

        criteriaQuery.where(predicate.toPredicate(productRoot, criteriaQuery, cb));

        return em.createQuery(criteriaQuery)
                .setHint(QueryHints.HINT_CACHEABLE, "true")
                .setHint(QueryHints.HINT_CACHE_REGION, "query.product-count.cache");
    }

    private Specification<Product> createCriteriaWhere(SearchVectorQuery searchVector, Long category, Map<String, ParameterExpression<?>> params) {

        return (root, query, cb) -> {
            List<Predicate> listPredicates = new ArrayList<>();

            var fts = cb.function(ProductPostgreSQLDialect.SEARCH_FUNCTION, Boolean.class, root.get(Product_.NAME_SEARCH), params.get("q1"));
            var equals1 = cb.equal(fts, params.get("b"));

            listPredicates.add(equals1);

            if (searchVector.attributesQuery().isPresent()) {
                var attributesJoin = root.join(Product_.ATTRIBUTES, JoinType.LEFT);
                var fts_attr = cb.function(ProductPostgreSQLDialect.SEARCH_FUNCTION, Boolean.class, attributesJoin.get(ProductAttribute_.ATTRIBUTE_SEARCH), params.get("q2"));
                var equals2 = cb.equal(fts_attr, params.get("b"));
                listPredicates.add(equals2);
            }

            if (category != null) {
                var categoriesJoin = root.join(Product_.CATEGORIES, JoinType.LEFT);
                var equals3 = cb.equal(categoriesJoin.get(Category_.ID), params.get("c"));
                listPredicates.add(equals3);
            }

            return cb.and(listPredicates.toArray(Predicate[]::new));
        };
    }
}
