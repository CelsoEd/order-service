package com.example.order.service;

import com.example.order.model.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class RedisCacheServiceTest {
    @Mock
    private RedisTemplate<String, Pedido> redisTemplate;

    @Mock
    private ValueOperations<String, Pedido> valueOperations; // Mock de ValueOperations

    @InjectMocks
    private RedisCacheService redisCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configura o mock para que opsForValue() retorne valueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCacheOrder() {
        Pedido pedido = Pedido.builder()
                .idUsuario("CUST123")
                .valorTotal(new BigDecimal("100.0"))
                .status("PROCESSED")
                .build();

        redisCacheService.cacheOrder("test:key", pedido);

        verify(valueOperations, times(1)).set("test:key", pedido, 24, java.util.concurrent.TimeUnit.HOURS);
    }

    @Test
    void testGetCachedOrder() {
        Pedido pedido = Pedido.builder()
                .idUsuario("CUST123")
                .valorTotal(new BigDecimal("100.0"))
                .status("PROCESSED")
                .build();

        when(valueOperations.get("test:key")).thenReturn(pedido);
        Pedido retrievedPedido = redisCacheService.getCachedOrder("test:key",  Pedido.class);

        assertNotNull(retrievedPedido);
        assertEquals("CUST123", retrievedPedido.getIdUsuario());
        verify(valueOperations, times(1)).get("test:key");
    }
}