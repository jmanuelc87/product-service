package com.ecommerce.products.util;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;


// TODO: Implement Thread-Safe LRU Cache
public class LRUCache<K, V> {

    private final Deque<K> doublyQueue;
    private final Map<K, CacheElement<V>> map;
    private final int CACHE_SIZE;

    public LRUCache(int cache_size) {
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
        map.put(key, new CacheElement<>(value));
    }

    public Optional<V> getOrEmpty(K key) {
        CacheElement<V> node;
        if (map.containsKey(key)) {
            node = map.get(key);
            node.lastAccessed = System.currentTimeMillis();
            doublyQueue.remove(key);
        } else {
            return Optional.empty();
        }
        doublyQueue.push(key);
        return Optional.ofNullable(node.value);
    }

    public void remove(K key) {
        doublyQueue.remove(key);
        map.remove(key);
    }

    public int size() {
        return doublyQueue.size();
    }

    public boolean isEmpty() {
        return doublyQueue.isEmpty();
    }

    public void clear() {
        doublyQueue.clear();
        map.clear();
    }

    protected static class CacheElement<V> {

        public final V value;
        public long lastAccessed;

        public CacheElement(V value) {
            this.value = value;
            lastAccessed = System.currentTimeMillis();
        }
    }
}
