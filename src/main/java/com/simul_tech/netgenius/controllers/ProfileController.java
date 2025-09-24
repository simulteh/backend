package com.simul_tech.netgenius.controllers;

import com.simul_tech.netgenius.dto.ProfileResponse;
import com.simul_tech.netgenius.impls.UserDetailsImpl;
import com.simul_tech.netgenius.security.JwtCore;
import com.simul_tech.netgenius.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile controller", description = "API для личного кабинета")
@RequiredArgsConstructor
public class ProfileController {
    private final JwtCore jwtCore;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getInfo(String token) throws UsernameNotFoundException {
        String email = jwtCore.getEmailFromJwt(token);
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) userService.loadUserByUsername(email);
            ProfileResponse profileResponse = new ProfileResponse(
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getMiddleName(),
                    userDetails.getEmail()

            );
            return ResponseEntity.ok(profileResponse);
        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
