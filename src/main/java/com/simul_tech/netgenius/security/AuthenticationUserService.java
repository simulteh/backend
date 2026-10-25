package com.simul_tech.netgenius.security;

import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Загрузка актуальных учётных данных для пароля и JWT без зависимостей от управления профилем. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationUserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetailsImpl loadUserByUsername(String email) {
        return userRepository.findByEmail(email)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("Неверные учётные данные"));
    }

    public UserDetailsImpl loadUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }
}
