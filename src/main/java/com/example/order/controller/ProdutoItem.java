package com.example.order.controller;

import lombok.Data;

@Data
public class ProdutoItem {
    private String id;
    private Integer quantidade;

    public ProdutoItem(String id, Integer quantidade) {
        this.id = id;
        this.quantidade = quantidade;
    }
}