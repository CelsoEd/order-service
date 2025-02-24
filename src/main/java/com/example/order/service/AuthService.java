package com.example.order.service;

import com.example.order.exception.MensagemErrorException;
import com.example.order.service.dto.LoginRequest;
import com.example.order.service.dto.LoginResponse;
import com.example.order.service.dto.RegistroRequest;
import com.example.order.service.dto.RegistroResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret.key}")
    private String secretKey;

    public AuthService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public LoginResponse authenticate(LoginRequest loginRequest) {
        logger.debug("Tentando autenticar usuário: {}", loginRequest.getIdUsuario());

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getIdUsuario());
        if (userDetails == null) {
            logger.warn("Usuário não encontrado: {}", loginRequest.getIdUsuario());
            throw new MensagemErrorException(HttpStatus.UNAUTHORIZED,
                    "Credenciais inválidas");
        }

        if (new BCryptPasswordEncoder().matches(loginRequest.getPassword(), userDetails.getPassword())) {
            logger.debug("Usuário autenticado com sucesso, gerando token para: {}", loginRequest.getIdUsuario());
            String token = generateToken(userDetails.getUsername());
            logger.debug("Token gerado: {}", token);
            return LoginResponse.builder().token(token).build();
        }
        logger.warn("Senha inválida para usuário: {}", loginRequest.getIdUsuario());
        throw new MensagemErrorException(HttpStatus.UNAUTHORIZED,
                "Credenciais inválidas");
    }

    public String generateToken(String idUsuario) {
        logger.debug("Gerando token para usuário: {}", idUsuario);
        return Jwts.builder()
                .setSubject(idUsuario)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas de validade
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey)))
                .compact();
    }

    public RegistroResponse registrar(RegistroRequest registroRequest) {
        logger.debug("Tentando registrar usuário: {}", registroRequest.getIdUsuario());

        UserService userService = (UserService) userDetailsService;
        userService.registrarUsuario(registroRequest.getIdUsuario(), registroRequest.getPassword());

        logger.debug("Usuário registrado com sucesso: {}", registroRequest.getIdUsuario());
        return RegistroResponse.builder()
                .idUsuario(registroRequest.getIdUsuario())
                .message("Usuário registrado com sucesso")
                .build();
    }
}