package com.example.order.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheOrder(String key, Object order) {
        redisTemplate.opsForValue().set(key, order, 24, TimeUnit.HOURS);
    }

    public void cacheOrder(String key, Object order, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, order, timeout, unit);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCachedOrder(String key, Class<T> type) {
        Object cached = redisTemplate.opsForValue().get(key);
        return cached != null ? (T) cached : null;
    }

    public void cacheProductInfo(String key, Object productInfo) {
        redisTemplate.opsForValue().set(key, productInfo, 5, TimeUnit.MINUTES); // TTL de 5 minutos
    }

    @SuppressWarnings("unchecked")
    public <T> T getCachedProductInfo(String key, Class<T> type) {
        Object cached = redisTemplate.opsForValue().get(key);
        return cached != null ? (T) cached : null;
    }
}