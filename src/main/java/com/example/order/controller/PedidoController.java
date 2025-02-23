package com.example.order.controller;

import com.example.order.service.dto.PedidoResponse;
import com.example.order.service.PedidoService;
import com.example.order.model.Produto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedido")
public class PedidoController {

    private final PedidoService pedidoService;


    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Endpoint 1: Buscar todos os produtos do External A
    @GetMapping("/listar-produtos")
    public ResponseEntity<List<Produto>> getAllProducts() {
        List<Produto> produtos = pedidoService.buscaTodosProdutos();
        return ResponseEntity.ok(produtos);
    }

    // Endpoint 2: Criar um pedido com idUsuario e lista de produtos (id e quantidade)
    @PostMapping("/fazer-pedido")
    public ResponseEntity<PedidoResponse> createBatchPedido(
            @RequestBody BatchPedidoRequest batchRequest) {
        PedidoResponse response = pedidoService.createBatchPedido(batchRequest.getIdUsuario(), batchRequest.getProducts());
        return ResponseEntity.ok(response);
    }

    // Novo Endpoint 3: Listar todos os pedidos de um usuário
    @GetMapping("/meus-pedidos/{idUsuario}")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorUsuario(@PathVariable String idUsuario) {
        List<PedidoResponse> pedidos = pedidoService.listarPedidosPorUsuario(idUsuario);
        if (pedidos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content se não houver pedidos
        }
        return ResponseEntity.ok(pedidos);
    }

}