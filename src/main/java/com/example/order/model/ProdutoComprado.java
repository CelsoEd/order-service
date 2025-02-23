package com.example.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoComprado {
    private String idProduto;
    private Integer quantidade;
    private String nomeProduto;
}