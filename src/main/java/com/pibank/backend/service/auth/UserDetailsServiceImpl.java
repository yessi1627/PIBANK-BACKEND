package com.pibank.backend.service.auth;

import com.pibank.backend.domain.entity.User;
import com.pibank.backend.domain.enums.UserStatus;
import com.pibank.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
            .accountExpired(false)
            .accountLocked(user.isAccountLocked() || user.getStatus() == UserStatus.BLOCKED)
            .credentialsExpired(false)
            .disabled(user.getStatus() == UserStatus.INACTIVE || user.getStatus() == UserStatus.SUSPENDED)
            .build();
    }
}
