package com.example.order.service;

import com.example.order.exception.MensagemErrorException;
import com.example.order.feignclient.ExternalAClient;
import com.example.order.model.Pedido;
import com.example.order.model.Produto;
import com.example.order.model.ProdutoComprado;
import com.example.order.repository.PedidoRepository;
import com.example.order.service.dto.PedidoResponse;
import com.example.order.controller.ProdutoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ExternalAClient externalAClient;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        // Configura o SecurityContextHolder para todos os testes
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void buscaTodosProdutos_deveRetornarListaDoCache_quandoCacheExistir() {
        // Arrange
        List<Produto> produtosCacheados = List.of(new Produto("1", "Produto1", BigDecimal.TEN, 10));
        when(redisCacheService.getCachedOrder("todosProdutos", List.class)).thenReturn(produtosCacheados);

        // Act
        List<Produto> result = pedidoService.buscaTodosProdutos();

        // Assert
        assertEquals(produtosCacheados, result);
        verify(redisCacheService).getCachedOrder("todosProdutos", List.class);
        // Não precisamos verificar o externalAClient, pois o teste espera que ele não seja chamado
    }

    @Test
    void buscaTodosProdutos_deveRetornarListaVazia_quandoExternalRetornarNulo() {
        // Arrange
        when(redisCacheService.getCachedOrder("todosProdutos", List.class)).thenReturn(null);
        when(externalAClient.getTodosProdutos()).thenReturn(null);

        // Act
        List<Produto> result = pedidoService.buscaTodosProdutos();

        // Assert
        assertTrue(result.isEmpty());
        verify(redisCacheService).getCachedOrder("todosProdutos", List.class);
        verify(externalAClient).getTodosProdutos();
        verify(redisCacheService, never()).cacheOrder(anyString(), any(), anyLong(), any());
    }

    @Test
    void buscaTodosProdutos_deveCachearERetornarLista_quandoExternalRetornarProdutos() {
        // Arrange
        List<Produto> produtos = List.of(new Produto("1", "Produto1", BigDecimal.TEN, 10));
        when(redisCacheService.getCachedOrder("todosProdutos", List.class)).thenReturn(null);
        when(externalAClient.getTodosProdutos()).thenReturn(produtos);

        // Act
        List<Produto> result = pedidoService.buscaTodosProdutos();

        // Assert
        assertEquals(produtos, result);
        verify(redisCacheService).getCachedOrder("todosProdutos", List.class);
        verify(externalAClient).getTodosProdutos();
        verify(redisCacheService).cacheOrder("todosProdutos", produtos, 5L, java.util.concurrent.TimeUnit.MINUTES);
    }

    @Test
    void createBatchPedido_deveCriarPedidoComSucesso_quandoProdutosDisponiveis() {
        // Arrange
        ProdutoItem item = new ProdutoItem("1", 2);
        List<ProdutoItem> produtos = List.of(item);
        Produto produto = new Produto("1", "Produto1", BigDecimal.TEN, 10);
        Pedido pedidoSalvo = new Pedido("user123");
        pedidoSalvo.setId("pedido123");
        pedidoSalvo.setStatus("PENDENTE PAGAMENTO");
        pedidoSalvo.setValorTotal(new BigDecimal("20.00"));
        pedidoSalvo.setHorarioCriacao(LocalDateTime.now());
        pedidoSalvo.setProdutosComprado(List.of(new ProdutoComprado("1", 2, "Produto1")));

        when(externalAClient.getQuantidadeProduto("1")).thenReturn(10);
        when(redisCacheService.getCachedOrder("produto:1", Produto.class)).thenReturn(produto);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoSalvo);

        // Act
        PedidoResponse response = pedidoService.createBatchPedido(produtos);

        // Assert
        assertNotNull(response);
        assertEquals("pedido123", response.getCodigoPedido());
        assertEquals("user123", response.getUsuario());
        assertEquals(new BigDecimal("20.00"), response.getValorTotal());
        assertEquals("PENDENTE PAGAMENTO", response.getSituacao());
        verify(externalAClient).getQuantidadeProduto("1");
        verify(redisCacheService).getCachedOrder("produto:1", Produto.class);
        verify(pedidoRepository).save(any(Pedido.class));
        verify(redisCacheService).cacheOrder("user123:batch", pedidoSalvo);
    }

    @Test
    void createBatchPedido_deveLancarExcecao_quandoProdutoNaoDisponivel() {
        // Arrange
        ProdutoItem item = new ProdutoItem("1", 5);
        List<ProdutoItem> produtos = List.of(item);
        when(externalAClient.getQuantidadeProduto("1")).thenReturn(3);

        // Act & Assert
        MensagemErrorException exception = assertThrows(MensagemErrorException.class, () -> {
            pedidoService.createBatchPedido(produtos);
        });
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Produto 1 não está disponível", exception.getMessage());
        verify(externalAClient).getQuantidadeProduto("1");
        verifyNoInteractions(pedidoRepository); // Não deve salvar nada
    }

    @Test
    void listarPedidosPorUsuario_deveRetornarListaDoCache_quandoCacheExistir() {
        // Arrange
        Pedido pedido = new Pedido("user123");
        pedido.setId("pedido123");
        pedido.setValorTotal(BigDecimal.TEN);
        pedido.setStatus("PENDENTE PAGAMENTO");
        pedido.setHorarioCriacao(LocalDateTime.now());
        List<Pedido> pedidosCacheados = List.of(pedido);
        when(redisCacheService.getCachedOrder("pedidos:user123", List.class)).thenReturn(pedidosCacheados);

        // Act
        List<PedidoResponse> result = pedidoService.listarPedidosPorUsuario();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("pedido123", result.get(0).getCodigoPedido());
        verify(redisCacheService).getCachedOrder("pedidos:user123", List.class);
        verifyNoInteractions(pedidoRepository);
    }

    @Test
    void listarPedidosPorUsuario_deveRetornarListaVazia_quandoNaoHouverPedidos() {
        // Arrange
        when(redisCacheService.getCachedOrder("pedidos:user123", List.class)).thenReturn(null);
        when(pedidoRepository.findByIdUsuario("user123")).thenReturn(Collections.emptyList());

        // Act
        List<PedidoResponse> result = pedidoService.listarPedidosPorUsuario();

        // Assert
        assertTrue(result.isEmpty());
        verify(redisCacheService).getCachedOrder("pedidos:user123", List.class);
        verify(pedidoRepository).findByIdUsuario("user123");
        verify(redisCacheService, never()).cacheOrder(anyString(), any(), anyLong(), any());
    }

    @Test
    void listarPedidosPorUsuario_deveCachearERetornarLista_quandoHouverPedidos() {
        // Arrange
        Pedido pedido = new Pedido("user123");
        pedido.setId("pedido123");
        pedido.setValorTotal(BigDecimal.TEN);
        pedido.setStatus("PENDENTE PAGAMENTO");
        pedido.setHorarioCriacao(LocalDateTime.now());
        List<Pedido> pedidos = List.of(pedido);
        when(redisCacheService.getCachedOrder("pedidos:user123", List.class)).thenReturn(null);
        when(pedidoRepository.findByIdUsuario("user123")).thenReturn(pedidos);

        // Act
        List<PedidoResponse> result = pedidoService.listarPedidosPorUsuario();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("pedido123", result.get(0).getCodigoPedido());
        verify(redisCacheService).getCachedOrder("pedidos:user123", List.class);
        verify(pedidoRepository).findByIdUsuario("user123");
        verify(redisCacheService).cacheOrder("pedidos:user123", pedidos, 1L, java.util.concurrent.TimeUnit.MINUTES);
    }
}