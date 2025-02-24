package com.example.order.service;

import com.example.order.exception.MensagemErrorException;
import com.example.order.model.Usuario;
import com.example.order.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Configura o userService para usar o mock
        userService = new UserService(usuarioRepository);
    }

    @Test
    void loadUserByUsername_deveRetornarUserDetails_quandoUsuarioExiste() {
        // Arrange
        String idUsuario = "user123";
        String password = "$2a$10$XDKjLuza1qYS/0qO0Md7OOr9Q0s2S1KumL9p/WmFP6W8Z2v3Nb0pa"; // Senha codificada (password123)
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setPassword(password);

        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.of(usuario));

        // Act
        UserDetails userDetails = userService.loadUserByUsername(idUsuario);

        // Assert
        assertNotNull(userDetails);
        assertEquals(idUsuario, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals("ROLE_USER", userDetails.getAuthorities().iterator().next().getAuthority());
        verify(usuarioRepository).findById(idUsuario);
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    void loadUserByUsername_deveLancarExcecao_quandoUsuarioNaoExiste() {
        // Arrange
        String idUsuario = "nonexistentUser";
        when(usuarioRepository.findById(idUsuario)).thenReturn(Optional.empty());

        // Act & Assert
        MensagemErrorException exception = assertThrows(MensagemErrorException.class, () -> {
            userService.loadUserByUsername(idUsuario);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Usuário não encontrado: " + idUsuario, exception.getMessage());
        verify(usuarioRepository).findById(idUsuario);
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    void registrarUsuario_deveSalvarUsuario_quandoNaoExiste() {
        // Arrange
        String idUsuario = "newUser";
        String password = "password123";
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setPassword(new BCryptPasswordEncoder().encode(password));

        when(usuarioRepository.existsByIdUsuario(idUsuario)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // Act
        userService.registrarUsuario(idUsuario, password);

        // Assert
        verify(usuarioRepository).existsByIdUsuario(idUsuario);
        verify(usuarioRepository).save(any(Usuario.class));
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    void registrarUsuario_deveLancarExcecao_quandoUsuarioJaExiste() {
        // Arrange
        String idUsuario = "existingUser";
        String password = "password123";
        when(usuarioRepository.existsByIdUsuario(idUsuario)).thenReturn(true);

        // Act & Assert
        MensagemErrorException exception = assertThrows(MensagemErrorException.class, () -> {
            userService.registrarUsuario(idUsuario, password);
        });
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Usuário já existe: " + idUsuario, exception.getMessage());
        verify(usuarioRepository).existsByIdUsuario(idUsuario);
        verifyNoMoreInteractions(usuarioRepository);
    }
}