package com.example.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {
    private String id;
    private String nome;
    private BigDecimal valor;
    private Integer quantidadeDisponivel;
}