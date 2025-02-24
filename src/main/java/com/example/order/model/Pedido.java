package com.example.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {
    @Id
    private String id;

    private String idUsuario;
    private BigDecimal valorTotal;
    private String status;
    private LocalDateTime horarioCriacao;
    private LocalDateTime horarioExpiracao;
    private List<ProdutoComprado> produtosComprado = new ArrayList<>();

    public Pedido(String idUsuario) {
        this.idUsuario = idUsuario;
        this.horarioCriacao = LocalDateTime.now();
        this.horarioExpiracao = horarioCriacao.plusMonths(1);
    }
}