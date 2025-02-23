package com.example.order.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BatchPedidoRequest {

    private String idUsuario;
    private List<ProdutoItem> products;

}