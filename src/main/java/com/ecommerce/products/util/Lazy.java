package com.ecommerce.products.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class Lazy<T> {

    private final AtomicReference<T> cached = new AtomicReference<>();
    private final Supplier<T> supplier;

    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "Supplier is null");
        return new Lazy<>(supplier);
    }

    public T get() {
        T value = cached.get();
        if (value == null) {
            synchronized (cached) {
                value = cached.get();
                if (value == null) {
                    value = supplier.get();
                    cached.set(value);
                }
            }
        }

        return value;
    }
}
