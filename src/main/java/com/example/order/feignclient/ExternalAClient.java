package com.example.order.feignclient;

import com.example.order.config.FeignConfig;
import com.example.order.model.Produto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "${app.feign.external-api.nome}", url = "${app.feign.external-api.url}", configuration = FeignConfig.class)
public interface ExternalAClient {

    @GetMapping("/api/produto/{idProduto}/quantidade")
    Integer getQuantidadeProduto(@PathVariable String idProduto);

    @GetMapping("/api/produto/{idProduto}/preco")
    Double getPrecoProduto(@PathVariable String idProduto);

    @GetMapping("/api/produto/listar-todos")
    List<Produto> getTodosProdutos();

    @GetMapping("/api/produtos/{idProduto}")
    Produto getProduto(String idProduto);
}