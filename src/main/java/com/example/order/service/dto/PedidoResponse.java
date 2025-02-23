package com.example.order.service.dto;

import com.example.order.model.ProdutoComprado;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PedidoResponse {
    private String id;
    private String idUsuario;
    private List<ProdutoComprado> produtosComprado;
    private BigDecimal totalValue;
    private String status;
    private String horarioPedido;
}