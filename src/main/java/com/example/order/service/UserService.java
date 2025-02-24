package com.example.order.service;

import com.example.order.exception.MensagemErrorException;
import com.example.order.model.Usuario;
import com.example.order.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String idUsuario) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new MensagemErrorException(HttpStatus.NOT_FOUND, "Usuário não encontrado: " + idUsuario));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return User.withUsername(usuario.getIdUsuario())
                .password(usuario.getPassword())
                .authorities(Collections.singletonList(authority))
                .build();
    }

    public void registrarUsuario(String idUsuario, String password) {
        if (usuarioRepository.existsByIdUsuario(idUsuario)) {
            throw new MensagemErrorException(HttpStatus.CONFLICT,"Usuário já existe: " + idUsuario);
        }

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setPassword(passwordEncoder.encode(password));
        usuarioRepository.save(usuario);
    }
}