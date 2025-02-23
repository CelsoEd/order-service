package com.example.order.config;

import com.example.order.feignclient.ExternalAClient;
import com.example.order.model.Produto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Profile("mock")
public class ExternalAMockConfig {

    @Bean("externalAMockClient")
    public ExternalAClient externalAMockClient() {
        return new ExternalAClient() {

            @Override
            public Double getPrecoProduto(String productId) {
                System.out.println("Mock: getProductPrice called for productId: " + productId);
                return "A123".equals(productId) ? 10.0 : "B123".equals(productId) ? 5.0 : null;
            }

            @Override
            public Integer getQuantidadeProduto(String productId) {
                System.out.println("Mock: getProductQuantity called for productId: " + productId);
                return "A123".equals(productId) ? 100 : "B123".equals(productId) ? 200 : 0;
            }

            @Override
            public Produto getProduto(String idProduto) {
                System.out.println("Mock: getProduto called for productId: " + idProduto);
                Map<String, Produto> produtos = new HashMap<>();
                produtos.put("A123", Produto.builder()
                        .id("A123")
                        .nome("Café")
                        .valor(new BigDecimal("10.0"))
                        .quantidadeDisponivel(100)
                        .build());
                produtos.put("B123", Produto.builder()
                        .id("B123")
                        .nome("Água")
                        .valor(new BigDecimal("5.0"))
                        .quantidadeDisponivel(200)
                        .build());

                return produtos.getOrDefault(idProduto, null);
            }

            @Override
            public List<Produto> getTodosProdutos() {
                System.out.println("Mock: getTodosProdutos called");
                return Arrays.asList(
                        Produto.builder()
                                .id("A123")
                                .nome("Café")
                                .valor(new BigDecimal("10.0"))
                                .quantidadeDisponivel(100)
                                .build(),
                        Produto.builder()
                                .id("B123")
                                .nome("Água")
                                .valor(new BigDecimal("5.0"))
                                .quantidadeDisponivel(200)
                                .build()
                );
            }
        };
    }
}