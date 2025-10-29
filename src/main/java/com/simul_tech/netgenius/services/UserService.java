package com.simul_tech.netgenius.services;

import com.simul_tech.netgenius.exceptions.EmailNotFound;
import com.simul_tech.netgenius.exceptions.UserNotFoundException;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.models.User;
import com.simul_tech.netgenius.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws EmailNotFound {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFound("Email not found: " + email));

        return UserDetailsImpl.build(user);
    }

    public User findByEmail(String email) throws EmailNotFound {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EmailNotFound("Email not found: " + email));
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User findById(int id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}
