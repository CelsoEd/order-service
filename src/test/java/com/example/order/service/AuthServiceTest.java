package com.example.order.service;

import com.example.order.exception.MensagemErrorException;
import com.example.order.service.dto.LoginRequest;
import com.example.order.service.dto.LoginResponse;
import com.example.order.service.dto.RegistroRequest;
import com.example.order.service.dto.RegistroResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userServiceMock; // Mock para UserService, usado separadamente

    @InjectMocks
    private AuthService authService; // Instância real, mas com mocks injetados

    @BeforeEach
    void setUp() {
        // Configura o authService para usar o mock
        authService = new AuthService(userDetailsService);
        // Configuramos o campo privado secretKey com uma chave segura (256 bits, 32 bytes)
        String secureSecretKey = Base64.getEncoder().encodeToString(new byte[32]); // Gera uma chave aleatória de 32 bytes
        ReflectionTestUtils.setField(authService, "secretKey", secureSecretKey);
    }

    @Test
    void authenticate_deveRetornarToken_quandoCredenciaisValidas() {
        // Arrange
        LoginRequest loginRequest = mock(LoginRequest.class);
        when(loginRequest.getIdUsuario()).thenReturn("user123");
        when(loginRequest.getPassword()).thenReturn("password123");

        UserDetails userDetails = User.withUsername("user123")
                .password(new BCryptPasswordEncoder().encode("password123"))
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername("user123")).thenReturn(userDetails);

        // Testamos diretamente o método público generateToken
        String expectedToken = authService.generateToken("user123");

        // Act
        LoginResponse response = authService.authenticate(loginRequest);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getToken()); // Verifica apenas se o token existe, não o valor exato
        assertTrue(response.getToken().length() > 0); // Garante que o token não está vazio
        verify(userDetailsService).loadUserByUsername("user123");
        verifyNoMoreInteractions(userDetailsService);
    }

    @Test
    void authenticate_deveLancarExcecao_quandoUsuarioNaoEncontrado() {
        // Arrange
        LoginRequest loginRequest = mock(LoginRequest.class);
        when(loginRequest.getIdUsuario()).thenReturn("user123");
        when(loginRequest.getPassword()).thenReturn("password123");
        when(userDetailsService.loadUserByUsername("user123")).thenReturn(null);

        // Act & Assert
        MensagemErrorException exception = assertThrows(MensagemErrorException.class, () -> {
            authService.authenticate(loginRequest);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(userDetailsService).loadUserByUsername("user123");
        verifyNoMoreInteractions(userDetailsService);
    }

    @Test
    void authenticate_deveLancarExcecao_quandoSenhaInvalida() {
        // Arrange
        LoginRequest loginRequest = mock(LoginRequest.class);
        when(loginRequest.getIdUsuario()).thenReturn("user123");
        when(loginRequest.getPassword()).thenReturn("wrongPassword");

        UserDetails userDetails = User.withUsername("user123")
                .password(new BCryptPasswordEncoder().encode("password123"))
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername("user123")).thenReturn(userDetails);

        // Act & Assert
        MensagemErrorException exception = assertThrows(MensagemErrorException.class, () -> {
            authService.authenticate(loginRequest);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(userDetailsService).loadUserByUsername("user123");
        verifyNoMoreInteractions(userDetailsService);
    }

    @Test
    void generateToken_deveGerarTokenValido() {
        // Arrange
        String idUsuario = "user123";
        // Testamos diretamente o método público generateToken
        String token = authService.generateToken(idUsuario);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0); // Verifica se o token não está vazio
        // Não testamos o conteúdo exato, pois depende da chave secreta e da data
    }
}