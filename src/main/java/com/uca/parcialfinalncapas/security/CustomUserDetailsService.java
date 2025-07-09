package com.uca.parcialfinalncapas.security;

import com.uca.parcialfinalncapas.entities.User;
import com.uca.parcialfinalncapas.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        User user = userRepo.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("No existe usuario con correo: " + correo));

        // Construimos el UserDetails con builder para mayor claridad
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getCorreo())
                .password(user.getPassword())
                .authorities(
                        Collections.singletonList(
                                new SimpleGrantedAuthority("ROLE_" + user.getNombreRol())
                        )
                )
                // marcamos explícitamente que la cuenta está activa y no bloqueada/expirada
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}