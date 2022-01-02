package com.ecommerce.products.util;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Slf4j
public class InMemoryCache<K, T> implements Closeable {

    private final int BETA;

    private final Duration timeToLive;

    private final LRUMap<K, CacheObject<T>> cacheMap;

    private final Random rand;

    private CleanupControlThread cleanupControlThread;

    public InMemoryCache(int beta, Duration timeToLive, final Duration timerInterval, int maxItems) {
        this.BETA = beta;
        this.timeToLive = timeToLive;
        this.cacheMap = new LRUMap<>(maxItems);
        this.rand = new Random();

        if (!timeToLive.isNegative() && !timerInterval.isNegative()) {
            cleanupControlThread = new CleanupControlThread(timerInterval, this::cleanup);
            cleanupControlThread.start();
        }
    }

    public void put(K key, T value) {
        synchronized (cacheMap) {
            cacheMap.put(key, new CacheObject<>(value));
        }
    }

    public Optional<T> get(K key) {
        synchronized (cacheMap) {
            var actualValue = cacheMap.getOrEmpty(key);

            if (actualValue.isEmpty()) {
                return Optional.empty();
            } else {
                actualValue.get().lastAccesed = System.currentTimeMillis();
                return Optional.of(actualValue.get().value);
            }
        }
    }

    public T getOrComputeIfAbsent(K key, Function<K, T> function) {
        var actualValue = cacheMap.getOrEmpty(key);

        if (actualValue.isEmpty() || xFetch(actualValue)) {
            synchronized (cacheMap) {
                actualValue = cacheMap.getOrEmpty(key);
                if (actualValue.isEmpty()) {
                    var start = System.currentTimeMillis();
                    var value = function.apply(key);
                    var delta = System.currentTimeMillis() - start;

                    var object = new CacheObject<>(value);
                    object.delta = delta;
                    object.expiryTime = object.lastAccesed + timeToLive.toMillis();

                    cacheMap.put(key, object);

                    log.info("Cache value computed from L2 or database. delta: {}, expiryTime: {}", object.delta, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").format(LocalDateTime.ofInstant(Instant.ofEpochMilli(object.expiryTime), ZoneOffset.UTC)));

                    return object.value;
                }
            }
        }

        // TODO: return promises or callable future for threads that received a cache miss and are waiting for a result instead of a failure
        return actualValue.get().value;
    }

    private boolean xFetch(Optional<CacheObject<T>> actualValue) {
        return System.currentTimeMillis() - actualValue.get().delta * this.BETA * Math.log(rand.nextDouble()) >= actualValue.get().expiryTime;
    }

    public void remove(K key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }

    public void cleanup() {
        log.info("Performing cleanup");
        long now = System.currentTimeMillis();
        List<K> deleteKeys = null;

        synchronized (cacheMap) {
            Iterator<K> itr = cacheMap.iterator();

            K key = null;
            Optional<CacheObject<T>> value = null;
            deleteKeys = new ArrayList<>((cacheMap.size() / 2) + 1);

            while (itr.hasNext()) {
                key = itr.next();
                value = cacheMap.getOrEmpty(key);

                if (value.isPresent() && (now > (timeToLive.toMillis() + value.get().lastAccesed))) {
                    deleteKeys.add(key);
                }
            }
        }

        for (K key : deleteKeys) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }

            Thread.yield();
        }
    }

    @Override
    public void close() throws IOException {
        if (cleanupControlThread.isRunning()) {
            cleanupControlThread.interrupt();
            log.info("Bye Bye cleanup control thread.");
        }
    }

    protected static class CacheObject<T> {

        public long lastAccesed;

        public long delta;

        public long expiryTime;

        public T value;

        protected CacheObject(T value) {
            this.value = value;
            this.lastAccesed = System.currentTimeMillis();
        }
    }

    private static class CleanupControlThread implements Runnable {

        private final Duration sleepInterval;

        private final AtomicBoolean running = new AtomicBoolean(false);

        private final AtomicBoolean stopped = new AtomicBoolean(true);

        private final Runnable action;

        private Thread worker;

        private CleanupControlThread(Duration sleepInterval, Runnable action) {
            this.sleepInterval = sleepInterval;
            this.action = action;
        }

        public void interrupt() {
            running.set(false);
            worker.interrupt();
            worker = null;
        }

        public void start() {
            worker = new Thread(this);
            worker.setDaemon(true);
            worker.start();
        }

        public boolean isRunning() {
            return running.get();
        }

        public boolean isStopped() {
            return stopped.get();
        }

        @Override
        public void run() {
            running.set(true);
            stopped.set(false);
            while (running.get()) {
                try {
                    Thread.sleep(sleepInterval.toMillis());
                    action.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Thread was interrupted, Failed to complete operation");
                }
            }

            stopped.set(true);
            log.info("Finishing cleanup control thread.");
        }
    }
}
