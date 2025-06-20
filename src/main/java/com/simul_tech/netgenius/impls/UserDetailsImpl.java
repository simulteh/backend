package com.simul_tech.netgenius.impls;

import com.simul_tech.netgenius.models.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Реализация UserDetails для интеграции с Spring Security
 * Используется в системе аутентификации модуля управления студентами
 */
@Data
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean active;

    /**
     * Создает UserDetailsImpl на основе сущности User
     * @param user сущность пользователя из базы данных
     * @return объект UserDetailsImpl
     */
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isActive());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(DEFAULT_ROLE));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}