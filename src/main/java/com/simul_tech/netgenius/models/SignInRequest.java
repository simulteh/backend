package com.simul_tech.netgenius.models;

import lombok.Data;

@Data
public class SignInRequest {
    private String username;
    private String password;
}
