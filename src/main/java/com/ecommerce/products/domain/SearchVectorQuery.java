package com.ecommerce.products.domain;

import com.ecommerce.products.util.Lazy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor(staticName = "of")
public class SearchVectorQuery {

    private static final String KEY_VALUE_PAIRS_DELIM = ",";

    private static final String KEY_VALUE_DELIM = ":";

    private static final String AND_JOINER = "&";

    private static final String OR_JOINER = "|";

    private String searchQuery;

    private String attributesQuery;

    public String searchQuery() {
        Lazy<String> lazy = Lazy.of(this::computeSearchQuery);
        return lazy.get();
    }

    public String computeSearchQuery() {
        String[] vector = searchQuery.split("\\W+");
        return String.join("<->", vector);
    }

    public Optional<String> attributesQuery() {
        if (attributesQuery == null || attributesQuery.isEmpty() || attributesQuery.isBlank()) {
            return Optional.empty();
        }

        Lazy<String> lazy = Lazy.of(this::computeAttributesQuery);
        return Optional.of(lazy.get());
    }

    public String computeAttributesQuery() {
        String[] keyValues = attributesQuery.split(KEY_VALUE_PAIRS_DELIM);
        return Arrays.stream(keyValues)
                .map(v -> v.split(KEY_VALUE_DELIM))
                .map(arr -> String.join(AND_JOINER, arr))
                .collect(Collectors.joining(OR_JOINER));
    }
}
