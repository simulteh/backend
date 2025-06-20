package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.UserNotFoundException;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по имени пользователя для аутентификации Spring Security
     * @param username имя пользователя или email
     * @return UserDetails объект с данными пользователя
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Попытка загрузки пользователя: {}", username);

        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {}", username);
                    return new UsernameNotFoundException("Неверные учетные данные");
                });

        if (!user.getIsActive()) {
            log.warn("Попытка входа неактивного пользователя: {}", username);
            throw new UsernameNotFoundException("Учетная запись неактивна");
        }

        log.debug("Пользователь успешно загружен: {}", username);
        return UserDetailsImpl.build(user);
    }

    /**
     * Получает активного пользователя по имени пользователя
     * @param username имя пользователя
     * @return найденного пользователя
     * @throws UserNotFoundException если пользователь не найден или неактивен
     */
    @Transactional(readOnly = true)
    public User getActiveUserByUsername(String username) throws UserNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        if (!user.getIsActive()) {
            throw new UserNotFoundException("Учетная запись неактивна: " + username);
        }

        return user;
    }
}