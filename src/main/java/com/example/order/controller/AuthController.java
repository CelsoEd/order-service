package com.example.order.controller;

import com.example.order.service.dto.LoginRequest;
import com.example.order.service.dto.LoginResponse;
import com.example.order.service.dto.RegistroRequest;
import com.example.order.service.dto.RegistroResponse;
import com.example.order.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registrar(@RequestBody RegistroRequest registroRequest) {
        RegistroResponse response = authService.registrar(registroRequest);
        return ResponseEntity.ok(response);
    }
}