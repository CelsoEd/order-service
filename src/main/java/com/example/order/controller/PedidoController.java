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

    @GetMapping("/listar-produtos")
    public ResponseEntity<List<Produto>> getAllProducts() {
        List<Produto> produtos = pedidoService.buscaTodosProdutos();
        return ResponseEntity.ok(produtos);
    }

    @PostMapping("/fazer-pedido")
    public ResponseEntity<PedidoResponse> createBatchPedido(
            @RequestBody BatchPedidoRequest batchRequest) {

        PedidoResponse response = pedidoService.createBatchPedido(batchRequest.getProdutoItems());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meus-pedidos")
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorUsuario() {
        List<PedidoResponse> pedidos = pedidoService.listarPedidosPorUsuario();
        if (pedidos.isEmpty()) {
            return ResponseEntity.noContent().build(); // Retorna 204 No Content se não houver pedidos
        }
        return ResponseEntity.ok(pedidos);
    }

}