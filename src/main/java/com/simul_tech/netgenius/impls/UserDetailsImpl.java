package com.simul_tech.netgenius.impls;

import com.simul_tech.netgenius.enums.Role;
import com.simul_tech.netgenius.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private final Long id;
    private final String email;
    private final String password;
    private final Role role;
    private final boolean emailConfirmed;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(User user) {
        Role role = user.getRole() != null ? user.getRole() : Role.USER;
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                role,
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        // Аутентификация разрешена и до подтверждения email; чувствительные
        // операции проверяют emailConfirmed отдельно.
        return true;
    }
}
