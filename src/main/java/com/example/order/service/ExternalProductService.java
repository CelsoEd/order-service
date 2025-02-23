package com.example.order.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ExternalProductService {
    public BigDecimal getProductPrice(String productId) {
        if (productId.startsWith("A")) {
            return new BigDecimal("100.00"); // Produto Externo A
        } else if (productId.startsWith("B")) {
            return new BigDecimal("150.00"); // Produto Externo B
        }
        throw new IllegalArgumentException("Produto não encontrado: " + productId);
    }

    public boolean isProductAvailable(String productId) {
        return true; // Simulação: todos os produtos estão disponíveis
    }
}