package com.example.order.service;

import com.example.order.service.dto.PedidoResponse;
import com.example.order.model.Pedido;
import com.example.order.model.PedidoRepository;
import com.example.order.model.Produto;
import com.example.order.model.ProdutoComprado;
import com.example.order.feignclient.ExternalAClient;
import com.example.order.controller.ProdutoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class PedidoServiceTest {

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ExternalAClient externalAClient;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
    }

    @Test
    void testBuscaTodosProdutos_Success() {
        String cacheKey = "todosProdutos";
        Produto produto1 = Produto.builder()
                .id("A123")
                .nome("Café")
                .valor(new BigDecimal("10.0"))
                .quantidadeDisponivel(100)
                .build();
        Produto produto2 = Produto.builder()
                .id("B123")
                .nome("Água")
                .valor(new BigDecimal("5.0"))
                .quantidadeDisponivel(200)
                .build();

        List<Produto> produtos = Arrays.asList(produto1, produto2);
        when(redisCacheService.getCachedOrder(cacheKey, List.class)).thenReturn(null);
        when(externalAClient.getTodosProdutos()).thenReturn(produtos);

        List<Produto> result = pedidoService.buscaTodosProdutos();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getId().equals("A123") && p.getNome().equals("Café")));
        assertTrue(result.stream().anyMatch(p -> p.getId().equals("B123") && p.getNome().equals("Água")));
        verify(redisCacheService, times(1)).cacheOrder(cacheKey, produtos, 5, TimeUnit.MINUTES);
    }

    @Test
    void testCreateBatchPedido_Success() {
        String idUsuario = "USER123";
        ProdutoItem item1 = new ProdutoItem("A123", 3);
        ProdutoItem item2 = new ProdutoItem("B123", 2);
        List<ProdutoItem> products = Arrays.asList(item1, item2);

        Produto produto1 = Produto.builder()
                .id("A123")
                .nome("Café")
                .valor(new BigDecimal("10.0"))
                .quantidadeDisponivel(100)
                .build();
        Produto produto2 = Produto.builder()
                .id("B123")
                .nome("Água")
                .valor(new BigDecimal("5.0"))
                .quantidadeDisponivel(200)
                .build();

        String cacheKey1 = "produto:A123";
        String cacheKey2 = "produto:B123";
        when(redisCacheService.getCachedOrder(cacheKey1, Produto.class)).thenReturn(null);
        when(redisCacheService.getCachedOrder(cacheKey2, Produto.class)).thenReturn(null);
        when(externalAClient.getProduto("A123")).thenReturn(produto1);
        when(externalAClient.getProduto("B123")).thenReturn(produto2);
        when(externalAClient.getQuantidadeProduto("A123")).thenReturn(100);
        when(externalAClient.getQuantidadeProduto("B123")).thenReturn(200);

        Pedido pedido = Pedido.builder()
                .idUsuario("USER123")
                .valorTotal(new BigDecimal("40.0"))
                .status("PENDENTE PAGAMENTO")
                .horarioCriacao(LocalDateTime.now())
                .build();
        pedido.getProdutosComprado().add(new ProdutoComprado("A123", 3, "Café"));
        pedido.getProdutosComprado().add(new ProdutoComprado("B123", 2, "Água"));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        PedidoResponse response = pedidoService.createBatchPedido(idUsuario, products);

        assertNotNull(response);
        assertEquals("USER123", response.getIdUsuario());
        assertEquals(new BigDecimal("40.0"), response.getTotalValue());
        assertEquals("PENDENTE PAGAMENTO", response.getStatus());
        assertNotNull(response.getHorarioPedido()); // Verifica se a data/hora está formatada
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(redisCacheService, times(1)).cacheOrder("USER123:batch", pedido);
        verify(redisCacheService, times(1)).cacheOrder(cacheKey1, produto1, 5, TimeUnit.MINUTES);
        verify(redisCacheService, times(1)).cacheOrder(cacheKey2, produto2, 5, TimeUnit.MINUTES);
    }

    @Test
    void testListarPedidosPorUsuario_Success() {
        String idUsuario = "USER123";
        Pedido pedido1 = Pedido.builder()
                .idUsuario("USER123")
                .valorTotal(new BigDecimal("40.0"))
                .status("PENDENTE PAGAMENTO")
                .horarioCriacao(LocalDateTime.of(2025, 2, 23, 3, 30))
                .build();
        pedido1.getProdutosComprado().add(new ProdutoComprado("A123", 3, "Café"));
        pedido1.getProdutosComprado().add(new ProdutoComprado("B123", 2, "Água"));

        Pedido pedido2 = Pedido.builder()
                .idUsuario("USER123")
                .valorTotal(new BigDecimal("20.0"))
                .status("PENDENTE PAGAMENTO")
                .horarioCriacao(LocalDateTime.of(2025, 2, 23, 4, 15))
                .build();
        pedido2.getProdutosComprado().add(new ProdutoComprado("A123", 2, "Café"));

        String cacheKey = "pedidos:" + idUsuario;
        when(redisCacheService.getCachedOrder(cacheKey, List.class)).thenReturn(null);
        when(pedidoRepository.findByIdUsuario(idUsuario)).thenReturn(Arrays.asList(pedido1, pedido2));

        List<PedidoResponse> responses = pedidoService.listarPedidosPorUsuario(idUsuario);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.stream().anyMatch(r -> r.getTotalValue().compareTo(new BigDecimal("40.0")) == 0));
        assertTrue(responses.stream().anyMatch(r -> r.getTotalValue().compareTo(new BigDecimal("20.0")) == 0));
        assertTrue(responses.stream().anyMatch(r -> r.getHorarioPedido().equals("23/02/2025 03:30:00")));
        assertTrue(responses.stream().anyMatch(r -> r.getHorarioPedido().equals("23/02/2025 04:15:00")));
        verify(redisCacheService, times(1)).cacheOrder(cacheKey, anyList(), 1, TimeUnit.MINUTES);
    }
}