package com.example.order.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistroRequest {
    private String idUsuario;
    private String password;
}