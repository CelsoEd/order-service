package com.example.order.repository;

import com.example.order.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    boolean existsByIdUsuario(String idUsuario);
}