package com.example.order.service.dto;

import com.example.order.model.ProdutoComprado;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PedidoResponse {
    private String codigoPedido;
    private String usuario;
    private List<ProdutoComprado> produtosComprado;
    private BigDecimal valorTotal;
    private String situacao;
    private String horarioPedido;
}