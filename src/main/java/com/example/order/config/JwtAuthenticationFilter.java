package com.example.order.config;

import com.example.order.exception.MensagemErrorException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Base64;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String[] PUBLIC_ENDPOINTS = {"/api/login", "/api/registro", "/api/pedido/listar-produtos"};
    private final UserDetailsService userDetailsService;
    private SecretKey secretKey;

    @Value("${jwt.secret.key}")
    private String secretKeyBase64;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void afterPropertiesSet() {
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyBase64));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.debug("Processando requisição para: {}", requestURI);

        // Ignorar endpoints públicos (como /api/login e /api/pedido/listar-produtos)
        for (String publicEndpoint : PUBLIC_ENDPOINTS) {
            if (requestURI.equals(publicEndpoint)) {
                logger.debug("Ignorando autenticação JWT para endpoint público: {}", requestURI);
                filterChain.doFilter(request, response);
                return;
            }
        }

        final String authorizationHeader = request.getHeader("Authorization");

        logger.debug("Authorization Header: {}", authorizationHeader);

        String username = null;
        String jwt = null;

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.warn("Cabeçalho de autorização ausente ou inválido: {}", authorizationHeader);
            throw new MensagemErrorException(HttpStatus.FORBIDDEN,
                    "Token JWT ausente ou inválido (formato 'Bearer <token>' esperado)");
        }

        jwt = authorizationHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
            username = claims.getSubject();
            logger.debug("Claims extraídos do token: {}", claims);
            logger.debug("Usuário extraído do token: {}", username);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
            throw new MensagemErrorException(HttpStatus.FORBIDDEN, "Token JWT expirado");
        } catch (io.jsonwebtoken.SignatureException e) {
            logger.error("Assinatura inválida do token JWT: {}, Detalhes: {}", jwt, e.getMessage());
            throw new MensagemErrorException(HttpStatus.FORBIDDEN, "Assinatura do token JWT inválida");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            logger.error("Token JWT malformado: {}, Detalhes: {}", jwt, e.getMessage());
            throw new MensagemErrorException(HttpStatus.FORBIDDEN, "Token JWT malformado");
        } catch (Exception e) {
            logger.error("Erro ao validar token JWT - Token: {}, Erro: {}", jwt, e.getMessage());
            throw new MensagemErrorException(HttpStatus.FORBIDDEN,
                    "Erro ao validar token JWT: " + e.getMessage());
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.debug("UserDetails carregados: {}", userDetails);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Usuário autenticado com sucesso: {}", username);
            } catch (UsernameNotFoundException e) {
                logger.error("Usuário não encontrado: {}", username);
                throw new MensagemErrorException(HttpStatus.FORBIDDEN, "Usuário não encontrado");
            }
        }

        filterChain.doFilter(request, response);
    }
}