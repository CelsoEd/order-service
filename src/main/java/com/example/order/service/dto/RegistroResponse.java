package com.example.order.service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistroResponse {
    private String idUsuario;
    private String message;
}