package com.example.order.config;

import com.example.order.model.Pedido;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
public class MongoConfig {

    public MongoConfig(MongoOperations mongoOperations) {
        IndexOperations indexOps = mongoOperations.indexOps(Pedido.class);

        // Criar índice TTL no campo expiresAt (expira imediatamente após expiresAt, ou seja, 6 meses)
        Index ttlIndex = new Index()
                .on("horarioExpiracao", Sort.Direction.ASC) // 1 para ASCENDING (ordem crescente)
                .expire(0L); // Expira imediatamente após expiresAt

        indexOps.ensureIndex(ttlIndex);
    }
}