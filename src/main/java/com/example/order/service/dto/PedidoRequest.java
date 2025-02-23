package com.example.order.service.dto;

import com.example.order.model.Produto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PedidoRequest {
    private String customerId;
    private List<Produto> products;
}