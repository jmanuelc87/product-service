package com.ecommerce.products.util;

import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;


public class LRUMap<K, V> implements Iterable<K> {

    private final Deque<K> doublyQueue;
    private final Map<K, V> map;
    private final int CACHE_SIZE;

    public LRUMap(int cache_size) {
        this.CACHE_SIZE = cache_size;
        this.doublyQueue = new LinkedList<>();
        this.map = new HashMap<>();
    }

    public void put(K key, V value) {
        if (map.containsKey(key)) {
            doublyQueue.remove(key);
        } else {
            if (doublyQueue.size() == CACHE_SIZE) {
                var last = doublyQueue.removeLast();
                map.remove(last);
            }
        }
        doublyQueue.push(key);
        map.put(key, value);
    }

    public Optional<V> getOrEmpty(K key) {
        V node;
        if (map.containsKey(key)) {
            node = map.get(key);
            doublyQueue.remove(key);
        } else {
            return Optional.empty();
        }
        doublyQueue.push(key);
        return Optional.ofNullable(node);
    }

    public void remove(K key) {
        doublyQueue.remove(key);
        map.remove(key);
    }

    public int size() {
        return doublyQueue.size();
    }

    public boolean isEmpty() {
        return doublyQueue.size() <= 0;
    }

    public boolean isFull() {
        return doublyQueue.size() >= CACHE_SIZE;
    }

    @Override
    public Iterator<K> iterator() {
        return map.keySet().iterator();
    }
}
