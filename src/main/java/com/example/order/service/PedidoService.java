package com.example.order.service;

import com.example.order.exception.MensagemErrorException;
import com.example.order.model.ProdutoComprado;
import com.example.order.service.dto.PedidoResponse;
import com.example.order.model.Pedido;
import com.example.order.repository.PedidoRepository;
import com.example.order.model.Produto;
import com.example.order.feignclient.ExternalAClient;
import com.example.order.controller.ProdutoItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final ExternalAClient externalAClient;
    private final RedisCacheService redisCacheService;

    private static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public PedidoService(PedidoRepository pedidoRepository,
                         @Qualifier("externalAMockClient") ExternalAClient externalAClient,
                         RedisCacheService redisCacheService) {
        this.pedidoRepository = pedidoRepository;
        this.externalAClient = externalAClient;
        this.redisCacheService = redisCacheService;
    }

    public List<Produto> buscaTodosProdutos() {
        String cacheKey = "todosProdutos";
        @SuppressWarnings("unchecked")
        List<Produto> cachedProducts = (List<Produto>) redisCacheService.getCachedOrder(cacheKey, List.class);

        if (cachedProducts != null) {
            return cachedProducts;
        }

        List<Produto> produtos = externalAClient.getTodosProdutos();
        if (produtos == null || produtos.isEmpty()) {
            return List.of();
        }

        // Cacheia os produtos com um TTL de 5 minutos para refletir mudanças no External A
        redisCacheService.cacheOrder(cacheKey, produtos, 5, TimeUnit.MINUTES);
        return produtos;
    }

    @Transactional
    public PedidoResponse createBatchPedido(List<ProdutoItem> products) {

        if (products == null || products.isEmpty()) {
            throw new MensagemErrorException(HttpStatus.BAD_REQUEST, "Nenhum produto foi informado");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idUsuario = authentication.getName();

        BigDecimal valorTotal = BigDecimal.ZERO;
        Pedido pedido = new Pedido(idUsuario);
        pedido.setStatus("PENDENTE PAGAMENTO");

        for (ProdutoItem item : products) {
            String idProduto = item.getId();
            Integer quantidadeSolicitada = item.getQuantidade();

            boolean isDisponivel = externalAClient.getQuantidadeProduto(idProduto) > quantidadeSolicitada;
            if (!isDisponivel) {
                throw new MensagemErrorException(HttpStatus.BAD_REQUEST,
                        "Produto " + idProduto + " não está disponível");
            }

            // Busca o produto no cache ou no External A
            String cacheKey = "produto:" + idProduto;
            Produto produto = redisCacheService.getCachedOrder(cacheKey, Produto.class);

            if (produto == null) {
                produto = externalAClient.getProduto(idProduto);
                // Cacheia o produto com TTL de 5 minutos
                redisCacheService.cacheOrder(cacheKey, produto, 5, TimeUnit.MINUTES);
            }

            // Calcula o valor total para este item
            BigDecimal itemTotal = produto.getValor().multiply(BigDecimal.valueOf(quantidadeSolicitada));
            valorTotal = valorTotal.add(itemTotal);

            // Adicione lógica para armazenar os produtos comprados
            pedido.getProdutosComprado().add(new ProdutoComprado(idProduto, quantidadeSolicitada, produto.getNome()));
        }

        pedido.setValorTotal(valorTotal);
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        String pedidoCacheKey = idUsuario + ":batch";
        redisCacheService.cacheOrder(pedidoCacheKey, pedidoSalvo);

        return mapToResponse(pedidoSalvo);
    }

    private PedidoResponse mapToResponse(Pedido pedido) {
        return PedidoResponse.builder()
                .codigoPedido(pedido.getId())
                .usuario(pedido.getIdUsuario())
                .produtosComprado(pedido.getProdutosComprado())
                .valorTotal(pedido.getValorTotal())
                .situacao(pedido.getStatus())
                .horarioPedido(pedido.getHorarioCriacao().format(BR_FORMATTER))
                .build();
    }

    public List<PedidoResponse> listarPedidosPorUsuario() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idUsuario = authentication.getName();

        String cacheKey = "pedidos:" + idUsuario;
        @SuppressWarnings("unchecked")
        List<Pedido> cachedPedidos = (List<Pedido>) redisCacheService.getCachedOrder(cacheKey, List.class);

        if (cachedPedidos != null) {
            return cachedPedidos.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }

        List<Pedido> pedidos = pedidoRepository.findByIdUsuario(idUsuario);
        if (pedidos.isEmpty()) {
            return List.of();
        }

        redisCacheService.cacheOrder(cacheKey, pedidos, 1, TimeUnit.MINUTES);
        return pedidos.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}